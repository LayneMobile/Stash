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
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func1;
import stash.internal.Util;
import stash.internal.stash.CombineTransformer;
import stash.predicates.EntryPredicate;
import stash.util.Result;

public class Stash<T> {
    private static final Stash EMPTY = Stash.create(new EmptyWorker());

    @NonNull private final StashWorker<T> worker;

    private final Func1<LazyEntry<T>, T> dataMapFunc = new Func1<LazyEntry<T>, T>() {
        @Override public T call(LazyEntry<T> entry) {
            T data = (entry == null) ? null : entry.getData();
            Util.closeQuietly(entry);
            return data;
        }
    };
    private final Func1<LazyEntry<T>, Metadata> metadataMapFunc = new Func1<LazyEntry<T>, Metadata>() {
        @Override public Metadata call(LazyEntry<T> lazyEntry) {
            Metadata metadata = (lazyEntry == null) ? null : lazyEntry.getMetadata();
            Util.closeQuietly(lazyEntry);
            return metadata;
        }
    };

    protected Stash(@NonNull Stash<T> stash) {
        this.worker = stash.worker;
    }

    protected Stash(@NonNull ObservableWorker<T> worker) {
        this.worker = new StashWorker<T>(worker);
    }

    protected Stash(@NonNull final Worker<T> worker) {
        this.worker = new StashWorker<T>(worker);
    }

    @NonNull public static <T> Stash<T> create(@NonNull Worker<T> worker) {
        return new Stash<T>(worker);
    }

    @NonNull public static <T> Stash<T> create(@NonNull ObservableWorker<T> worker) {
        return new Stash<T>(worker);
    }

    @NonNull public static <T> Stash<T> empty() {
        return (Stash<T>) EMPTY;
    }

    @NonNull public final Request<Entry<T>> get() {
        return Request.from(getInternal().map(new Func1<Entry<T>, Entry<T>>() {
            @Override public Entry<T> call(Entry<T> entry) {
                if (entry != null) {
                    // Fully read lazy entry, and return a copy
                    return Entry.Builder.copy(entry);
                }
                return null;
            }
        }));
    }

    @NonNull public final Request<T> getData() {
        return getLazy().map(dataMapFunc);
    }

    @NonNull public final Request<Metadata> getMetadata() {
        return getLazy().map(metadataMapFunc);
    }

    @NonNull public final Request<Entry<T>> get(@NonNull final EntryPredicate<T> predicate) {
        return getLazy(predicate).map(new Func1<LazyEntry<T>, Entry<T>>() {
            @Override public Entry<T> call(LazyEntry<T> entry) {
                if (entry != null) {
                    // Fully read lazy entry, and return a copy
                    return Entry.Builder.copy(entry);
                }
                return null;
            }
        });
    }

    @NonNull public final Request<T> getData(@NonNull EntryPredicate<T> predicate) {
        return getLazy().map(dataMapFunc);
    }

    @NonNull public final Request<LazyEntry<T>> getLazy() {
        return Request.from(getInternal().map(new Func1<Entry<T>, LazyEntry<T>>() {
            @Override public LazyEntry<T> call(Entry<T> entry) {
                return LazyEntry.cast(entry);
            }
        }));
    }

    @NonNull public final Request<LazyEntry<T>> getLazy(@NonNull final EntryPredicate<T> predicate) {
        Observable<LazyEntry<T>> observable = getLazy().asObservable().filter(new Func1<LazyEntry<T>, Boolean>() {
            @Override public Boolean call(LazyEntry<T> lazyEntry) {
                boolean keep = !predicate.isExpired(lazyEntry);
                if (!keep) {
                    Util.closeQuietly(lazyEntry);
                }
                return keep;
            }
        }).defaultIfEmpty(null);
        return Request.from(observable);
    }

    @NonNull public final Request<T> put(@NonNull T data) {
        return put(new Entry.Builder<T>()
                .setData(data)
                .build());
    }

    @NonNull public final Request<T> put(@Nullable List<String> metadata, @NonNull T data) {
        return put(new Entry.Builder<T>()
                .setMetadata(metadata)
                .setData(data)
                .build());
    }

    @NonNull public final Request<T> put(@NonNull Metadata metadata, @NonNull T data) {
        return put(new Entry.Builder<T>()
                .setMetadata(metadata)
                .setData(data)
                .build());
    }

    @NonNull public final Request<T> put(@NonNull final Entry<T> entry) {
        return Request.from(worker.put(entry));
    }

    @NonNull public final Request<T> overwrite(@NonNull final T data) {
        return getMetadata().concatMap(new Func1<Metadata, Observable<? extends T>>() {
            @Override public Observable<? extends T> call(Metadata metadata) {
                return put(metadata, data).asObservable();
            }
        });
    }

    @NonNull public final Request<Boolean> remove() {
        return Request.from(worker.remove());
    }

    @NonNull public final <R> Stash<R> compose(@NonNull Transformer<T, R> transformer) {
        return new Stash<R>(transformer.call(worker));
    }

    @NonNull public final Stash<T> combine(@NonNull Stash<T> secondary) {
        return compose(new CombineTransformer<T>(secondary.worker));
    }

    @NonNull private Observable<Entry<T>> getInternal() {
        return worker.get();
    }

    public interface Worker<T> {
        @Nullable Entry<T> get() throws Exception;

        @Nullable T put(@NonNull Entry<T> entry) throws Exception;

        boolean remove() throws Exception;
    }

    public interface ObservableWorker<T> {
        @NonNull Observable<Entry<T>> get();

        @NonNull Observable<T> put(@NonNull Entry<T> entry);

        @NonNull Observable<Boolean> remove();
    }

    public interface Transformer<T, R> extends Func1<StashWorker<T>, ObservableWorker<R>> {}

    public static final class StashWorker<T> {
        private final ObservableWorker<T> worker;

        private StashWorker(final Worker<T> worker) {
            if (worker == null) throw new IllegalArgumentException("worker must not be null");
            this.worker = new ObservableWorker<T>() {
                @NonNull @Override public Observable<Entry<T>> get() {
                    return StashObservables.from(new Callable<Entry<T>>() {
                        @Override public Entry<T> call() throws Exception {
                            return worker.get();
                        }
                    });
                }

                @NonNull @Override public Observable<T> put(@NonNull final Entry<T> entry) {
                    return StashObservables.from(new Callable<T>() {
                        @Override public T call() throws Exception {
                            return worker.put(entry);
                        }
                    });
                }

                @NonNull @Override public Observable<Boolean> remove() {
                    return StashObservables.from(new Callable<Boolean>() {
                        @Override public Boolean call() throws Exception {
                            return worker.remove();
                        }
                    });
                }
            };
        }

        private StashWorker(ObservableWorker<T> worker) {
            if (worker == null) throw new IllegalArgumentException("worker must not be null");
            this.worker = worker;
        }

        @NonNull public Observable<Entry<T>> get() {
            return worker.get();
        }

        @NonNull public Observable<Result<Entry<T>>> getResult() {
            return result(worker.get());
        }

        @NonNull public Observable<T> put(@NonNull Entry<T> entry) {
            return worker.put(entry);
        }

        @NonNull public Observable<Result<T>> putResult(@NonNull Entry<T> entry) {
            return result(worker.put(entry));
        }

        @NonNull public Observable<Boolean> remove() {
            return worker.remove();
        }

        @NonNull public Observable<Result<Boolean>> removeResult() {
            return result(worker.remove());
        }

        @NonNull private static <T> Observable<Result<T>> result(Observable<T> observable) {
            return observable.map(new Func1<T, Result<T>>() {
                @Override public Result<T> call(T t) {
                    return Result.success(t);
                }
            }).onErrorReturn(new Func1<Throwable, Result<T>>() {
                @Override public Result<T> call(Throwable throwable) {
                    return Result.failure(throwable);
                }
            });
        }
    }

    private static final class EmptyWorker<T> implements Worker<T> {
        @Nullable @Override public Entry<T> get() throws Exception {
            return null;
        }

        @Nullable @Override public T put(@NonNull Entry<T> entry) throws Exception {
            return null;
        }

        @Override public boolean remove() throws Exception {
            return true;
        }
    }
}
