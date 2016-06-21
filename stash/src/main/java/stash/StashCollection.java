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

package stash;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func1;

public class StashCollection<K, V> {
    private final Worker<K, V> worker;

    protected StashCollection(@NonNull Worker<K, V> worker) {
        this.worker = worker;
    }

    public static <K, V> StashCollection<K, V> create(@NonNull Worker<K, V> worker) {
        return new StashCollection<K, V>(worker);
    }

    public final Stash<V> getStash(@NonNull StashKey<? extends K> stashKey) {
        return worker.getStash(stashKey);
    }

    public final Request<Entry<V>> getEntry(@NonNull StashKey<? extends K> stashKey) {
        return worker.getStash(stashKey).get();
    }

    public final Request<V> getData(@NonNull StashKey<? extends K> stashKey) {
        return worker.getStash(stashKey).getData();
    }

    public final Request<KeyStash<K, V>> getAll() {
        return from(new Callable<Iterable<? extends KeyStash<K, V>>>() {
            @Override public Iterable<? extends KeyStash<K, V>> call() throws Exception {
                return worker.getAll();
            }
        });
    }

    public final Request<StashEntry<K, Entry<V>>> getAllEntries() {
        Observable<StashEntry<K, Entry<V>>> observable = getAll()
                .asObservable()
                .flatMap(new Func1<KeyStash<K, V>, Observable<? extends StashEntry<K, Entry<V>>>>() {
                    @Override
                    public Observable<? extends StashEntry<K, Entry<V>>> call(final KeyStash<K, V> tkKeyStash) {
                        return tkKeyStash.get().asObservable().map(new Func1<Entry<V>, StashEntry<K, Entry<V>>>() {
                            @Override public StashEntry<K, Entry<V>> call(Entry<V> tEntry) {
                                return new StashEntry<K, Entry<V>>(tkKeyStash.getKey(), tEntry);
                            }
                        });
                    }
                });
        return Request.from(observable);
    }

    public final Request<StashEntry<K, V>> getAllData() {
        Observable<StashEntry<K, V>> observable
                = getAllEntries().asObservable().map(new Func1<StashEntry<K, Entry<V>>, StashEntry<K, V>>() {
            @Override public StashEntry<K, V> call(StashEntry<K, Entry<V>> kEntryStashEntry) {
                final Entry<V> value = kEntryStashEntry.getValue();
                return new StashEntry<K, V>(kEntryStashEntry.getKey(), value == null ? null : value.getData());
            }
        });
        return Request.from(observable);
    }

    public final Request<K> keys() {
        return from(new Callable<Iterable<? extends K>>() {
            @Override public Iterable<? extends K> call() throws Exception {
                return worker.keys();
            }
        });
    }

    public final Request<Boolean> removeAll() {
        return Request.from(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return worker.removeAll();
            }
        });
    }

    public final Request<Boolean> removeAll(@NonNull final Collection<StashKey<? extends K>> stashKeys) {
        return Request.from(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return worker.removeAll(stashKeys);
            }
        });
    }

    public final Request<Integer> size() {
        return Request.from(new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                return worker.size();
            }
        });
    }

    private static <V> Request<V> from(Callable<Iterable<? extends V>> callable) {
        Observable<V> observable = StashObservables.from(callable)
                .concatMap(new Func1<Iterable<? extends V>, Observable<? extends V>>() {
                    @Override public Observable<? extends V> call(Iterable<? extends V> ts) {
                        return Observable.from(ts);
                    }
                });
        return Request.from(observable);
    }

    public static final class StashEntry<K, V> {
        private final K k;
        private final V v;

        public StashEntry(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public K getKey() {
            return k;
        }

        public V getValue() {
            return v;
        }
    }

    public interface Worker<K, V> {
        @NonNull Stash<V> getStash(@NonNull StashKey<? extends K> stashKey);

        @NonNull Iterable<? extends K> keys();

        @NonNull Iterable<KeyStash<K, V>> getAll();

        boolean removeAll();

        boolean removeAll(Collection<StashKey<? extends K>> stashKeys);

        int size();
    }
}
