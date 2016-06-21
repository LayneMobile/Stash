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

package stash.internal;

import android.support.annotation.NonNull;
import android.util.LruCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import stash.Entry;
import stash.KeyStash;
import stash.Stash;
import stash.StashCollection;
import stash.StashKey;
import stash.stashdbs.MemDb;

class MemDbImpl implements MemDb.Worker {
    public static final int DEFAULT_SIZE = 5;

    private final Map<Class<?>, LruCache<Object, ? extends Entry<?>>> cache
            = new HashMap<Class<?>, LruCache<Object, ? extends Entry<?>>>(4);

    private MemDbImpl() { }

    static MemDb create() {
        return MemDb.create(new MemDbImpl());
    }

    @NonNull @Override public <V> StashCollection<Object, V> getCollection(@NonNull final Class<V> type) {
        return StashCollection.create(new CollectionWorker<V>(type));
    }

    @NonNull @Override public <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<?> stashKey) {
        return Stash.create(new Worker<V>(type, stashKey.getKey()));
    }

    @NonNull @Override public Iterable<?> keys() {
        synchronized (cache) {
            Collection<Object> keys = new HashSet<Object>();
            for (LruCache<Object, ? extends Entry<?>> cache : MemDbImpl.this.cache.values()) {
                keys.addAll(cache.snapshot().keySet());
            }
            return keys;
        }
    }

    @Override public boolean removeAll() {
        synchronized (cache) {
            for (LruCache cache : this.cache.values()) {
                cache.evictAll();
            }
            return true;
        }
    }

    @Override public boolean removeAll(@NonNull Collection<StashKey<?>> stashKeys) {
        synchronized (cache) {
            boolean removed = false;
            for (LruCache<Object, ?> cache : this.cache.values()) {
                for (StashKey<?> stashKey : stashKeys) {
                    Object key = stashKey.getKey();
                    removed |= cache.remove(key) != null;
                }
            }
            return removed;
        }
    }

    @Override
    public <T> void registerMaxSize(@NonNull Class<T> type, int maxSize) {
        final LruCache<Object, Entry<T>> old = getOrCreateCache(type, maxSize);
        if (old.maxSize() != maxSize) {
            final LruCache<Object, Entry<T>> cache = new LruCache<Object, Entry<T>>(maxSize);
            final Map<Object, Entry<T>> snapshot = old.snapshot();
            final int diff = old.maxSize() - maxSize;
            int i = 0;
            synchronized (old) {
                // snapshot is ordered from least recently used to most recently used
                for (Map.Entry<Object, Entry<T>> entry : snapshot.entrySet()) {
                    if (i++ < diff) {
                        continue;
                    }
                    cache.put(entry.getKey(), entry.getValue());
                }
            }

            synchronized (this.cache) {
                this.cache.put(type, cache);
            }
            old.evictAll();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> LruCache<Object, Entry<T>> getCache(Class<T> type) {
        synchronized (cache) {
            return (LruCache<Object, Entry<T>>) this.cache.get(type);
        }
    }

    private <T> LruCache<Object, Entry<T>> getOrCreateCache(Class<T> type, int maxSize) {
        synchronized (cache) {
            LruCache<Object, Entry<T>> cache = getCache(type);
            if (cache == null) {
                cache = new LruCache<Object, Entry<T>>(maxSize);
                this.cache.put(type, cache);
            }
            return cache;
        }
    }

    private class CollectionWorker<V> implements StashCollection.Worker<Object, V> {
        private final Class<V> type;

        public CollectionWorker(Class<V> type) {
            this.type = type;
        }

        @NonNull @Override public Stash<V> getStash(@NonNull StashKey<?> stashKey) {
            return Stash.create(new Worker<V>(type, stashKey.getKey()));
        }

        @NonNull @Override public Iterable<?> keys() {
            LruCache<?, ?> cache = getCache();
            if (cache != null) {
                return cache.snapshot().keySet();
            }
            return Collections.emptySet();
        }

        @NonNull @Override public Iterable<KeyStash<Object, V>> getAll() {
            final LruCache<?, ?> cache = getCache();
            if (cache == null) {
                return Collections.emptySet();
            }
            return new Iterable<KeyStash<Object, V>>() {
                @Override public Iterator<KeyStash<Object, V>> iterator() {
                    return new Iterator<KeyStash<Object, V>>() {
                        final Iterator<?> keys = cache.snapshot().keySet().iterator();

                        @Override public boolean hasNext() {
                            return keys.hasNext();
                        }

                        @Override public KeyStash<Object, V> next() {
                            Object next = keys.next();
                            return KeyStash.create(new Worker<V>(type, next), next);
                        }

                        @Override public void remove() {
                            throw new UnsupportedOperationException("read only iterator");
                        }
                    };
                }
            };
        }

        @Override public boolean removeAll() {
            LruCache<?, ?> cache = getCache();
            if (cache != null) {
                cache.evictAll();
                return true;
            }
            return false;
        }

        @Override public boolean removeAll(Collection<StashKey<?>> stashKeys) {
            LruCache<Object, ?> cache = getCache();
            if (cache != null) {
                boolean removed = false;
                for (StashKey<?> stashKey : stashKeys) {
                    Object key = stashKey.getKey();
                    removed |= cache.remove(key) != null;
                }
                return removed;
            }
            return false;
        }

        @Override public int size() {
            LruCache<?, ?> cache = getCache();
            if (cache != null) {
                return cache.size();
            }
            return 0;
        }

        private LruCache<Object, Entry<V>> getCache() {
            return MemDbImpl.this.getCache(type);
        }
    }

    private class Worker<T> implements Stash.Worker<T> {
        private final Class<T> type;
        private final Object key;

        private Worker(Class<T> type, Object key) {
            this.type = type;
            this.key = key;
        }

        @Override public Entry<T> get() throws Exception {
            LruCache<Object, Entry<T>> cache = getCache(type);
            if (cache != null) {
                return cache.get(key);
            }
            return null;
        }

        @Override public T put(@NonNull Entry<T> entry) throws Exception {
            LruCache<Object, Entry<T>> cache = getOrCreateCache(type, DEFAULT_SIZE);
            cache.put(key, entry);
            return getData(cache);
        }

        @Override public boolean remove() throws Exception {
            LruCache<Object, Entry<T>> cache = getCache(type);
            return cache != null && cache.remove(key) != null;
        }

        private T getData(LruCache<Object, Entry<T>> cache) {
            if (cache != null) {
                Entry<T> entry = cache.get(key);
                return entry == null ? null : entry.getData();
            }
            return null;
        }
    }
}
