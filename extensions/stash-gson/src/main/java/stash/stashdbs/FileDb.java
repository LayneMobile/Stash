/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stash.stashdbs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import stash.KeyStash;
import stash.LazyEntry;
import stash.Stash;
import stash.StashCollection;
import stash.StashDb;
import stash.StashKey;
import stash.internal.StashLog;

public class FileDb implements StashDb.Worker<String> {
    private static final String TAG = FileDb.class.getSimpleName();

    private final FileCache cache;
    private final Converter converter;

    protected FileDb(FileCache cache, Converter converter) {
        this.cache = cache;
        this.converter = converter;
    }

    public static FileDb open(FileCache.Config config,
            Converter converter) throws IOException {
        return new FileDb(FileCache.open(config), converter);
    }

    private static FileDb open(FileCache cache, Converter converter) {
        return new FileDb(cache, converter);
    }

    private static int readInt(BufferedReader source) throws IOException {
        String line = source.readLine();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new IOException("Expected an integer but was \"" + line + "\"");
        }
    }

    private static long readLong(BufferedReader source) throws IOException {
        String line = source.readLine();
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            throw new IOException("Expected a long but was \"" + line + "\"");
        }
    }

    private static void abortQuietly(FileCache.Editor editor) {
        // Give up because the cache cannot be written.
        try {
            if (editor != null) {
                editor.abort();
            }
        } catch (IOException ignored) {
            // ignore
        }
    }

    @NonNull @Override public <V> StashCollection<String, V> getCollection(@NonNull Class<V> type) {
        return StashCollection.create(new CollectionWorker<V>(type));
    }

    @NonNull @Override
    public <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<? extends String> stashKey) {
        return Stash.create(new Worker<V>(type, getKey(type, stashKey)));
    }

    @NonNull @Override public Iterable<? extends String> keys() {
        // TODO:
        return Collections.emptySet();
    }

    @Override public boolean removeAll() {
        // TODO:
        return false;
    }

    @Override public boolean removeAll(@NonNull Collection<StashKey<? extends String>> stashKeys) {
        // TODO:
        return false;
    }

    public boolean isClosed() {
        return cache.isClosed();
    }

    public void close() throws IOException {
        cache.close();
    }

    public static interface Converter {
        <T> T fromFile(Class<T> clazz, InputStream in) throws IOException;

        <T> void toFile(OutputStream out, T data) throws IOException;
    }

    private static final class Metadata implements stash.Metadata {
        private final long lastUpdated;
        private final List<String> data;

        private static Metadata create(InputStream in) throws IOException {
            final BufferedReader source = new BufferedReader(new InputStreamReader(in));
            try {
                long lastUpdated = readLong(source);
                final int size = readInt(source);
                List<String> data = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    data.add(source.readLine());
                }
                return new Metadata(lastUpdated, data);
            } finally {
                source.close();
            }
        }

        private Metadata(stash.Metadata meta) {
            this(meta == null ? System.currentTimeMillis() : meta.getLastUpdated(),
                    meta == null ? null : meta.getData());
        }

        private Metadata(List<String> data) {
            this(System.currentTimeMillis(), data);
        }

        private Metadata(long lastUpdated, List<String> data) {
            this.lastUpdated = lastUpdated;
            this.data = data;
        }

        @Override public long getLastUpdated() {
            return lastUpdated;
        }

        @NonNull @Override public List<String> getData() {
            return data == null ? Collections.<String>emptyList() : data;
        }

        private void writeTo(FileCache.Editor editor) throws IOException {
            OutputStream out = editor.newMetadata();
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.write(Long.toString(getLastUpdated()));
            writer.write('\n');

            final List<String> data = getData();
            final int size = data.size();
            writer.write(Integer.toString(size));
            writer.write('\n');
            for (int i = 0; i < size; i++) {
                writer.write(data.get(i));
                writer.write('\n');
            }

            writer.close();
        }
    }

    private static final class Body<T> {
        private final T data;

        private Body(T data) {
            this.data = data;
        }

        private void writeTo(FileCache.Editor editor, Converter converter) throws IOException {
            OutputStream out = editor.newBody();
            converter.toFile(out, data);
            out.close();
        }
    }

    private static final class Entry<T> extends LazyEntry<T> {
        private final Class<T> type;
        private final Converter converter;
        private final FileCache.Entry entry;

        private Entry(Class<T> type, Converter converter, FileCache.Entry entry) {
            this.type = type;
            this.converter = converter;
            this.entry = entry;
        }

        @NonNull @Override protected stash.Metadata loadMetadata() {
            try {
                return Metadata.create(entry.getMetadata());
            } catch (IOException e) {
                StashLog.e(TAG, "error loading metadata from file cache", e);
                return stash.Metadata.EMPTY;
            }
        }

        @Nullable @Override protected T loadData() {
            try {
                return converter.fromFile(type, entry.getBody());
            } catch (IOException e) {
                StashLog.e(TAG, "error loading data from file cache", e);
                return null;
            }
        }

        @Override protected void closeInternal() throws IOException {
            entry.close();
        }
    }

    private static <T> String getKey(Class<T> type, StashKey<? extends String> stashKey) {
        // TODO:
        return type.getName() + "." + stashKey.getKey();
    }

    private final class CollectionWorker<T> implements StashCollection.Worker<String, T> {
        private final Class<T> type;

        public CollectionWorker(Class<T> type) {
            this.type = type;
        }

        @NonNull @Override public Iterable<KeyStash<String, T>> getAll() {
            // TODO:
            return Collections.emptySet();
        }

        @NonNull @Override public Stash<T> getStash(@NonNull StashKey<? extends String> stashKey) {
            return FileDb.this.getStash(type, stashKey);
        }

        @NonNull @Override public Iterable<? extends String> keys() {
            // TODO:
            return Collections.emptySet();
        }

        @Override public boolean removeAll() {
            return false;
        }

        @Override public boolean removeAll(Collection<StashKey<? extends String>> stashKeys) {
            return false;
        }

        @Override public int size() {
            return 0;
        }
    }

    private final class Worker<T> implements Stash.Worker<T> {
        private final Class<T> type;
        private final String key;

        private Worker(Class<T> type, String key) {
            this.type = type;
            this.key = key;
        }

        @Override public stash.Entry<T> get() throws Exception {
            FileCache.Entry entry = cache.get(key);
            return (entry == null) ? null : new Entry<T>(type, converter, entry);
        }

        @Override public T put(@NonNull stash.Entry<T> entry) throws Exception {
            StashLog.d(TAG, "putting key: %s, entry: %s", key, entry);
            final Metadata meta = new Metadata(entry.getMetadata());
            final Body<T> body = new Body<T>(entry.getData());
            FileCache.Editor editor = null;
            try {
                editor = cache.edit(key);
                if (editor != null) {
                    meta.writeTo(editor);
                    body.writeTo(editor, converter);
                    editor.commit();
                    return body.data;
                }
                return null;
            } catch (IOException e) {
                StashLog.e(TAG, "error outputting", e);
                abortQuietly(editor);
                throw e;
            }
        }

        @Override public boolean remove() throws Exception {
            try {
                return cache.remove(key);
            } catch (IOException e) {
                StashLog.e(TAG, "error removing key: " + key, e);
                return false;
            }
        }
    }
}
