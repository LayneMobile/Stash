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

public abstract class StashDb<K> implements Functions.StashDb<K> {

    protected StashDb() {}

    public static <K> StashDb<K> create(@NonNull final Worker<K> worker) {
        return new Impl<K>(worker);
    }

    @NonNull public abstract <V> StashCollection<K, V> getStashCollection(@NonNull Class<V> type);

    @NonNull public abstract <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<? extends K> stashKey);

    @NonNull public abstract Request<K> keys();

    @NonNull public abstract Request<Boolean> removeAll();

    @NonNull public abstract Request<Boolean> removeAll(@NonNull Collection<StashKey<? extends K>> stashKeys);

    @NonNull @Override public final <T> Functions.StashCollection<T, K> stashCollectionFunction() {
        return new Functions.StashCollection<T, K>() {
            @NonNull @Override public StashCollection<K, T> getStashCollection(@NonNull Class<T> type) {
                return StashDb.this.getStashCollection(type);
            }
        };
    }

    @NonNull @Override public final <T> Functions.Stash<T, K> stashFunction() {
        return new Functions.Stash<T, K>() {
            @NonNull @Override
            public Stash<T> getStash(@NonNull Class<T> type, @NonNull StashKey<? extends K> stashKey) {
                return StashDb.this.getStash(type, stashKey);
            }
        };
    }

    public interface Worker<K> {
        @NonNull <V> StashCollection<K, V> getCollection(@NonNull Class<V> type);

        @NonNull <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<? extends K> stashKey);

        @NonNull Iterable<? extends K> keys();

        boolean removeAll();

        boolean removeAll(@NonNull Collection<StashKey<? extends K>> stashKeys);
    }

    private static class Impl<K> extends StashDb<K> {
        private final Worker<K> worker;

        private Impl(Worker<K> worker) {
            this.worker = worker;
        }

        @NonNull @Override public <V> StashCollection<K, V> getStashCollection(@NonNull Class<V> type) {
            return worker.getCollection(type);
        }

        @NonNull @Override
        public <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<? extends K> stashKey) {
            return worker.getStash(type, stashKey);
        }

        @NonNull @Override public Request<K> keys() {
            Observable<K> observable = StashObservables.from(new Callable<Iterable<? extends K>>() {
                @Override public Iterable<? extends K> call() throws Exception {
                    return worker.keys();
                }
            }).concatMap(new Func1<Iterable<? extends K>, Observable<? extends K>>() {
                @Override public Observable<? extends K> call(Iterable<? extends K> ks) {
                    return Observable.from(ks);
                }
            });
            return Request.from(observable);
        }

        @NonNull @Override public Request<Boolean> removeAll() {
            return Request.from(new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    return worker.removeAll();
                }
            });
        }

        @NonNull @Override
        public Request<Boolean> removeAll(@NonNull final Collection<StashKey<? extends K>> stashKeys) {
            return Request.from(new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    return worker.removeAll(stashKeys);
                }
            });
        }
    }
}
