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

package stash.internal.stash;


import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.exceptions.CompositeException;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.Entry;
import stash.Stash;
import stash.util.Result;

public final class CombineTransformer<T> implements Stash.Transformer<T, T> {
    private final Stash.StashWorker<T> secondary;

    public CombineTransformer(Stash.StashWorker<T> secondary) {
        this.secondary = secondary;
    }

    @Override public Stash.ObservableWorker<T> call(Stash.StashWorker<T> primary) {
        return new CombineWorker<T>(primary, secondary);
    }

    private static final class CombineWorker<T> implements Stash.ObservableWorker<T> {
        private final Stash.StashWorker<T> primary;
        private final Stash.StashWorker<T> secondary;

        private CombineWorker(Stash.StashWorker<T> primary, Stash.StashWorker<T> secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @NonNull @Override public Observable<Entry<T>> get() {
            return primary.getResult().concatMap(new Func1<Result<Entry<T>>, Observable<? extends Entry<T>>>() {
                @Override
                public Observable<? extends Entry<T>> call(final Result<Entry<T>> entryResult) {
                    Entry<T> entry = entryResult.getData();
                    if (entry != null) {
                        return Observable.just(entry);
                    }
                    return secondary.get().concatMap(new Func1<Entry<T>, Observable<? extends Entry<T>>>() {
                        @Override
                        public Observable<? extends Entry<T>> call(Entry<T> entry) {
                            if (entry == null) {
                                Throwable t = entryResult.getThrowable();
                                if (t != null) {
                                    return Observable.error(t);
                                }
                                return Observable.just(null);
                            }

                            // Save concrete entry to primary stash and return result
                            final Entry<T> concrete = Entry.Builder.copy(entry);
                            return primary.putResult(concrete).map(new Func1<Result<T>, Entry<T>>() {
                                @Override public Entry<T> call(Result<T> tResult) {
                                    return concrete;
                                }
                            });
                        }
                    });
                }
            });
        }

        @NonNull @Override public Observable<T> put(@NonNull Entry<T> entry) {
            Observable<Result<T>> p = primary.putResult(entry);
            Observable<Result<T>> s = secondary.putResult(entry);
            return zip(p, s);
        }

        @NonNull @Override public Observable<Boolean> remove() {
            Observable<Result<Boolean>> p = primary.removeResult();
            Observable<Result<Boolean>> s = secondary.removeResult();
            return zip(p, s);
        }

        @NonNull private static <T> Observable<T> zip(Observable<Result<T>> p, Observable<Result<T>> s) {
            return Observable.zip(p, s, new Func2<Result<T>, Result<T>, Result<T>>() {
                @Override public Result<T> call(Result<T> primary, Result<T> secondary) {
                    if (primary.isSuccess() && secondary.isSuccess()) {
                        T p = primary.getData();
                        if (p != null) {
                            return Result.success(p);
                        }
                        return Result.success(secondary.getData());
                    } else if (primary.isSuccess()) {
                        return Result.failure(secondary.getThrowable());
                    } else if (secondary.isSuccess()) {
                        return Result.failure(primary.getThrowable());
                    }
                    List<Throwable> tList = Arrays.asList(primary.getThrowable(), secondary.getThrowable());
                    return Result.failure(new CompositeException(tList));
                }
            }).concatMap(new Func1<Result<T>, Observable<? extends T>>() {
                @Override public Observable<? extends T> call(Result<T> result) {
                    if (result.isSuccess()) {
                        return Observable.just(result.getData());
                    }
                    return Observable.error(result.getThrowable());
                }
            });
        }
    }
}
