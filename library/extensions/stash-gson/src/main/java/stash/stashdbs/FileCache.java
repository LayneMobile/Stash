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

import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import stash.internal.StashLog;

public class FileCache implements Closeable {
    private static final String TAG = FileCache.class.getSimpleName();
    private static final String DEFAULT_DIR = "stash";
    private static final int DEFAULT_VERSION = 1;
    private static final int ENTRY_METADATA = 0;
    private static final int ENTRY_BODY = 1;
    private static final int ENTRY_COUNT = 2;
    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Singletons of this cache. For testing, we use an alternate cache directory, so it's not enough to have a global
     * singleton; we need to have a singleton for each cache dir.
     */
    private static final Map<File, Instance> sInstances = new HashMap<File, Instance>(5);

    private final DiskLruCache cache;
    private final Config config;

    private FileCache(Config config) throws IOException {
        this.config = config;
        this.cache = DiskLruCache.open(config.dir, config.version, ENTRY_COUNT, config.maxSize);
    }

    public static synchronized FileCache open(final Config config) throws IOException {
        Instance instance = sInstances.get(config.dir);
        if (instance == null) {
            StashLog.d(TAG, "creating cache in dir: %s", config.dir);
            instance = new Instance(new FileCache(config));
            sInstances.put(config.dir, instance);
        } else if (!instance.cache.config.equals(config)) {
            final int version = instance.cache.config.version;
            final int size = instance.cache.config.maxSize;
            if (version != config.version) {
                throw new IOException(
                        "Attempting to create an already existing cache in same dir with a different version. Current version: " +
                                version +
                                ", attempted version: " +
                                config.version);
            } else if (size != config.maxSize) {
                throw new IOException(
                        "Attempting to create an already existing cache in same dir with a different maxSize. Current maxSize: " +
                                size +
                                ", attempted maxSize: " +
                                config.maxSize);
            } else {
                throw new IOException("Error creating cache");
            }
        }
        instance.incrementOpenCount();
        StashLog.v(TAG, "%s open count incremented to %d", instance.cache, instance.openCount);
        return instance.cache;
    }

    public static File getDefaultDir(Context context) {
        return new File(context.getCacheDir(), DEFAULT_DIR);
    }

    private static synchronized void close(FileCache networkCache) throws IOException {
        final File cacheDir = networkCache.getDirectory();
        final Instance instance = sInstances.get(cacheDir);
        if (instance == null || instance.cache != networkCache) {
            networkCache.cache.close();
            return;
        }

        int openCount = instance.decrementOpenCount();
        StashLog.v(TAG, "%s open count decremented to %d", instance.cache, openCount);
        if (openCount <= 0) {
            instance.cache.cache.close();
            sInstances.remove(cacheDir);
        }
    }

    private static synchronized void delete(FileCache networkCache) throws IOException {
        final File cacheDir = networkCache.getDirectory();
        final Instance instance = sInstances.get(cacheDir);
        if (instance == null || instance.cache != networkCache) {
            networkCache.cache.delete();
            return;
        }

        instance.cache.cache.delete();
        sInstances.remove(cacheDir);
    }

    /**
     * Returns a 32 character string containing a hash of {@code s}.
     */
    private static String hash(String s) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] md5bytes = messageDigest.digest(s.getBytes("UTF-8"));
            return hex(md5bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns this byte string encoded in hexadecimal.
     */
    public static String hex(byte[] data) {
        char[] result = new char[data.length * 2];
        int c = 0;
        for (byte b : data) {
            result[c++] = HEX_DIGITS[(b >> 4) & 0xf];
            result[c++] = HEX_DIGITS[b & 0xf];
        }
        return new String(result);
    }

    private static String toKey(String key) {
        final String finalKey = hash(key.toUpperCase(Locale.US));
        StashLog.d(TAG, "toKey(key: %s) -> %s", key, finalKey);
        return finalKey;
    }

    public FileCache open() throws IOException {
        return open(config);
    }


    public Entry get(String key) {
        key = toKey(key);
        DiskLruCache.Snapshot snapshot;
        try {
            snapshot = cache.get(key);
            if (snapshot == null) {
                StashLog.v(TAG, "couldn't find snapshot for key: %s", key);
                return null;
            }
        } catch (IOException e) {
            StashLog.e(TAG, "error getting snapshot", e);
            // Give up because the cache cannot be read.
            return null;
        }
        return new Entry(snapshot);
    }

    public Editor edit(String key) throws IOException {
        key = toKey(key);
        DiskLruCache.Editor editor = cache.edit(key);
        StashLog.d(TAG, "edit(key: %s) -> editor: %s", key, editor);
        return (editor == null) ? null : new Editor(editor);
    }

    public boolean remove(String key) throws IOException {
        key = toKey(key);
        boolean success = cache.remove(key);
        StashLog.d(TAG, "remove(key: %s) -> success? %s", key, success);
        return success;
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete all files in the cache directory
     * including files that weren't created by the cache.
     */
    public void delete() throws IOException {
        delete(this);
    }

    public long getSize() {
        return cache.size();
    }

    public long maxSize() {
        return config.maxSize;
    }

    public void flush() throws IOException {
        cache.flush();
    }

    @Override
    public void close() throws IOException {
        close(this);
    }

    public File getDirectory() {
        return cache.getDirectory();
    }

    public boolean isClosed() {
        return cache.isClosed();
    }

    public static final class Entry implements Closeable {
        private final DiskLruCache.Snapshot snapshot;

        private Entry(DiskLruCache.Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        public InputStream getMetadata() {
            return snapshot.getInputStream(ENTRY_METADATA);
        }

        public InputStream getBody() {
            return snapshot.getInputStream(ENTRY_BODY);
        }

        public Editor edit() throws IOException {
            DiskLruCache.Editor editor = snapshot.edit();
            return (editor == null) ? null : new Editor(editor);
        }

        @Override
        public void close() throws IOException {
            snapshot.close();
        }
    }


    public static final class Editor {
        private final DiskLruCache.Editor editor;

        private Editor(DiskLruCache.Editor editor) {
            this.editor = editor;
        }

        public OutputStream newMetadata() throws IOException {
            return editor.newOutputStream(ENTRY_METADATA);
        }

        public OutputStream newBody() throws IOException {
            return editor.newOutputStream(ENTRY_BODY);
        }

        public void commit() throws IOException {
            editor.commit();
        }

        public void abort() throws IOException {
            editor.abort();
        }
    }

    public static class Config {
        private final File dir;
        private final int version;
        private final int maxSize;

        public Config(File dir, int version, int maxSize) {
            if (dir == null) {
                throw new IllegalStateException("Config directory cannot be null");
            }
            this.dir = dir;
            this.version = version;
            this.maxSize = maxSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Config)) return false;

            Config config = (Config) o;

            return maxSize == config.maxSize
                    && version == config.version
                    && dir.equals(config.dir);
        }

        @Override
        public int hashCode() {
            int result = dir.hashCode();
            result = 31 * result + version;
            result = 31 * result + maxSize;
            return result;
        }

        public static Config getDefault(Context context) {
            return new Config(getDefaultDir(context), DEFAULT_VERSION, 10 * 1024);
        }
    }


    private static final class Instance {
        private final FileCache cache;
        private int openCount;

        private Instance(FileCache cache) {
            this.cache = cache;
        }

        private void incrementOpenCount() {
            openCount++;
        }

        private int decrementOpenCount() {
            return --openCount;
        }
    }
}
