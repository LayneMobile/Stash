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

package stash.internal.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.functions.Func1;
import stash.Entry;
import stash.LazyEntry;
import stash.Metadata;
import stash.Progress;
import stash.Stash;
import stash.StashPolicy;
import stash.Stashable;
import stash.internal.StashLog;
import stash.internal.Util;
import stash.predicates.EntryPredicate;

class StashRunner<T> {
    private static final String TAG = StashRunner.class.getSimpleName();

    private final Stashable<T> stashable;
    private final Stash<T> stash;
    private final StashPolicy stashPolicy;

    @Nullable static <T> StashRunner<T> create(@Nullable Stashable<T> stashable, @NonNull StashPolicy stashPolicy) {
        if (stashable != null) {
            Stash<T> stash = stashable.getStash();
            if (stash != null) {
                return new StashRunner<T>(stashable, stash, stashPolicy);
            }
        }
        return null;
    }

    private StashRunner(@NonNull Stashable<T> stashable, @NonNull Stash<T> stash, @NonNull StashPolicy stashPolicy) {
        this.stashable = stashable;
        this.stash = stash;
        this.stashPolicy = stashPolicy;
    }

    Observable<StashState<T>> getState() {
        return get().map(new Func1<LazyEntry<T>, StashState<T>>() {
            @Override public StashState<T> call(LazyEntry<T> lazyEntry) {
                boolean isExpired;
                boolean isValid;
                Metadata meta = null;
                T data = null;
                try {
                    // First check if expired
                    isExpired = lazyEntry == null;
                    if (!isExpired) {
                        EntryPredicate<T> expiredPredicate = stashable.isExpired();
                        if (expiredPredicate != null) {
                            isExpired = expiredPredicate.isExpired(lazyEntry);
                        }
                    }
                    StashLog.d(TAG, "isExpired? %s", String.valueOf(isExpired));

                    // Now check if cache is valid
                    isValid = !isExpired;
                    if (isValid || stashPolicy == StashPolicy.STASH_THEN_SOURCE_IF_EXPIRED) {
                        if (lazyEntry != null) {
                            meta = lazyEntry.getMetadata();
                            data = lazyEntry.getData();
                        }
                        isValid = data != null;
                    }
                } finally {
                    // cleanup (possibly didn't read all of the data)
                    Util.closeQuietly(lazyEntry);
                    StashLog.d(TAG, "closed lazy entry");
                }

                // now return observable with correct return values
                if (stashPolicy == StashPolicy.STASH_ONLY_NO_SOURCE) {
                    return StashState.stash(meta, data);
                } else if (isValid) {
                    if (isExpired || stashPolicy == StashPolicy.STASH_THEN_SOURCE) {
                        // STASH_THEN_SOURCE_IF_EXPIRED || STASH_THEN_SOURCE
                        return StashState.stashAndSource(meta, data);
                    } else {
                        return StashState.stash(meta, data);
                    }
                }
                return StashState.source();
            }
        });
    }

    Observable<T> getData() {
        return getState().map(new Func1<StashState<T>, T>() {
            @Override public T call(StashState<T> tStashState) {
                return tStashState.stashData;
            }
        });
    }

    Observable<Progress<T>> getDataWithProgress() {
        return getData().map(new Func1<T, Progress<T>>() {
            @Override public Progress<T> call(T t) {
                return Progress.receivedFromStash(t);
            }
        });
    }

    SourceProgressProcessor.ConcatFunction<T> saveFunctionWithProgress() {
        return new SourceProgressProcessor.ConcatFunction<T>() {
            @Override public Observable<? extends Progress<T>> call(Progress<T> progress) {
                if (progress.getStatus() == Progress.Status.RECEIVED_FROM_SOURCE) {
                    Progress<T> saving = Progress.savingToStash();
                    Observable<Progress<T>> observable = Observable.just(progress, saving);
                    final T t = progress.getData();
                    Entry<T> entry = new Entry.Builder<T>()
                            .setMetadata(stashable.getMetadataToSave(t))
                            .setData(t)
                            .build();
                    Observable<Progress<T>> save = stash.put(entry)
                            .asObservable()
                            .onErrorReturn(new Func1<Throwable, T>() {
                                @Override public T call(Throwable throwable) {
                                    StashLog.e(TAG, "error saving to stash", throwable);
                                    return t;
                                }
                            }).map(new Func1<T, Progress<T>>() {
                                @Override public Progress<T> call(T t) {
                                    return Progress.savedToStash(t);
                                }
                            });
                    return observable.concatWith(save);
                }
                return Observable.just(progress);
            }
        };
    }

    private Observable<LazyEntry<T>> get() {
        return stash.getLazy().asObservable().onErrorReturn(new Func1<Throwable, LazyEntry<T>>() {
            @Override public LazyEntry<T> call(Throwable throwable) {
                StashLog.e(TAG, "error getting from stash", throwable);
                return null;
            }
        });
    }
}
