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

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import stash.Entry;
import stash.Params;
import stash.Progress;
import stash.Request;
import stash.Source;
import stash.SourceProcessor;
import stash.StashPolicy;
import stash.Stashable;
import stash.StashableProcessor;
import stash.params.StashableParams;
import stash.sources.AggregableSource;
import stash.sources.OpenEndedSource;
import stash.sources.StashableSource;
import stash.util.Result;

final class DefaultRequestProcessor<T, P extends Params> {
    private static final String TAG = DefaultRequestProcessor.class.getSimpleName();

    private final Observable<Progress<T>> executing = Observable.just(Progress.<T>executing());
    private final Observable<Progress<T>> fetching = Observable.just(Progress.<T>fetchingFromStash());
    private final Observable<Progress<T>> complete = Observable.just(Progress.<T>complete());

    private final SourceProgressProcessor<T, P> sourceProcessor;
    private final StashableProcessor<T, P> stashableProcessor;

    private DefaultRequestProcessor(
            @NonNull SourceProgressProcessor<T, P> sourceProcessor,
            @NonNull StashableProcessor<T, P> stashableProcessor) {
        this.sourceProcessor = sourceProcessor;
        this.stashableProcessor = stashableProcessor;
    }

    @NonNull
    static <T, P extends Params> DefaultRequestProcessor<T, P> create(@NonNull final Source<T, P> source) {
        final SourceProgressProcessor<T, P> sourceProcessor;
        if (source instanceof OpenEndedSource) {
            sourceProcessor = OpenEndedProcessor.create((OpenEndedSource<T, P>) source);
        } else if (source instanceof AggregableSource) {
            sourceProcessor = AggregableProcessor.create((AggregableSource<T, P>) source);
        } else {
            sourceProcessor = SourceProgressProcessor.create(source);
        }
        StashableProcessor<T, P> stashableProcessor = new StashableProcessorImpl<T, P>(source);
        return new DefaultRequestProcessor<T, P>(sourceProcessor, stashableProcessor);
    }

    @NonNull static <T, P extends Params> DefaultRequestProcessor<T, P> create(
            @NonNull SourceProgressProcessor<T, P> sourceProcessor,
            @NonNull StashableProcessor<T, P> stashableProcessor) {
        return new DefaultRequestProcessor<T, P>(sourceProcessor, stashableProcessor);
    }

    @NonNull static <T, P extends Params> DefaultRequestProcessor<T, P> create(
            @NonNull SourceProcessor<T, P> sourceProcessor,
            @NonNull StashableProcessor<T, P> stashableProcessor) {
        SourceProgressProcessor<T, P> source = SourceProgressProcessor.create(sourceProcessor);
        return new DefaultRequestProcessor<T, P>(source, stashableProcessor);
    }

    @NonNull public Request<T> getRequest(@NonNull P p) {
        return Request.from(getRequestObservable(p));
    }

    @NonNull public Request<Progress<T>> getProgressRequest(@NonNull P p) {
        return Request.from(getProgressRequestObservable(p));
    }

    @NonNull private Observable<T> getRequestObservable(@NonNull final P p) {
        final Blueprint<T> bp = getBlueprint(p);
        if (bp.steps == Blueprint.Steps.None) {
            return Observable.just(null);
        } else if (bp.steps == Blueprint.Steps.StashOnly) {
            return bp.stashRunner.getData();
        } else if (bp.steps == Blueprint.Steps.SourceOnly) {
            return getSourceRequest(p);
        } else if (bp.steps == Blueprint.Steps.SourceAndSaveOnly) {
            return getSourceRequest(p, bp.stashRunner);
        } else if (bp.steps == Blueprint.Steps.SourceAndSave_OnErrorStash) {
            return getSourceRequest(p, bp.stashRunner).map(new Func1<T, Result<T>>() {
                @Override public Result<T> call(T t) {
                    return Result.success(t);
                }
            }).onErrorReturn(new Func1<Throwable, Result<T>>() {
                @Override public Result<T> call(Throwable throwable) {
                    return Result.failure(throwable);
                }
            }).concatMap(new Func1<Result<T>, Observable<? extends T>>() {
                @Override public Observable<? extends T> call(
                        final Result<T> tResult) {
                    if (tResult.isSuccess() && tResult.getData() != null) {
                        return Observable.just(tResult.getData());
                    }
                    return bp.stashRunner.getData().concatMap(new Func1<T, Observable<? extends T>>() {
                        @Override public Observable<? extends T> call(T t) {
                            if (t == null) {
                                return Observable.error(tResult.getException());
                            }
                            return Observable.just(t);
                        }
                    });
                }
            });
        }

        // Default scenario, return depending on stash state
        return bp.stashRunner.getState().concatMap(new Func1<StashState<T>, Observable<? extends T>>() {
            @Override public Observable<? extends T> call(StashState<T> stashState) {
                if (stashState.kind == StashState.Kind.Stash) {
                    Observable<T> stash = Observable.just(stashState.stashData);
                    Observable<T> source = peekSourceRequest(p, stashState.stashEntry, bp.stashRunner);
                    if (source != null) {
                        return Observable.concat(stash, source);
                    }
                    return stash;
                }
                Observable<T> source = getSourceRequest(p, bp.stashRunner);
                if (stashState.kind == StashState.Kind.Source) {
                    return source;
                } else {
                    Observable<T> stash = Observable.just(stashState.stashData);
                    return Observable.concat(stash, source);
                }
            }
        });
    }

    @NonNull private Observable<Progress<T>> getProgressRequestObservable(@NonNull final P p) {
        final Blueprint<T> bp = getBlueprint(p);
        final List<Observable<Progress<T>>> observables = new ArrayList<Observable<Progress<T>>>(6);
        observables.add(executing);
        if (bp.steps == Blueprint.Steps.None) {
            observables.add(Observable.just(Progress.<T>receivedFromStash(null)));
        } else if (bp.steps == Blueprint.Steps.StashOnly) {
            observables.add(fetching);
            observables.add(bp.stashRunner.getDataWithProgress());
        } else if (bp.steps == Blueprint.Steps.SourceOnly) {
            observables.add(getSourceRequestWithProgress(p));
        } else if (bp.steps == Blueprint.Steps.SourceAndSaveOnly) {
            observables.add(getSourceRequestWithProgress(p, bp.stashRunner));
        } else if (bp.steps == Blueprint.Steps.SourceAndSave_OnErrorStash) {
            Observable<Progress<T>> next = getSourceRequestWithProgress(p, bp.stashRunner)
                    .map(new Func1<Progress<T>, Result<Progress<T>>>() {
                        @Override public Result<Progress<T>> call(Progress<T> tProgress) {
                            return Result.success(tProgress);
                        }
                    }).onErrorReturn(new Func1<Throwable, Result<Progress<T>>>() {
                        @Override public Result<Progress<T>> call(Throwable throwable) {
                            return Result.failure(throwable);
                        }
                    }).concatMap(new Func1<Result<Progress<T>>, Observable<? extends Progress<T>>>() {
                        @Override public Observable<? extends Progress<T>> call(
                                final Result<Progress<T>> progressResult) {
                            Progress<T> progress = progressResult.getData();
                            if (progressResult.isSuccess()) {
                                if (progress.getStatus() != Progress.Status.RECEIVED_FROM_SOURCE
                                        || progress.getData() != null) {
                                    return Observable.just(progressResult.getData());
                                }
                            }
                            final Throwable throwable = progressResult.getThrowable() != null
                                    ? progressResult.getThrowable()
                                    : new NullPointerException("data from source was null");
                            Observable<Progress<T>> stash = bp.stashRunner.getDataWithProgress()
                                    .concatMap(new Func1<Progress<T>, Observable<? extends Progress<T>>>() {
                                        @Override public Observable<? extends Progress<T>> call(Progress<T> tProgress) {
                                            if (tProgress.getStatus() == Progress.Status.RECEIVED_FROM_STASH
                                                    && tProgress.getData() == null) {
                                                return Observable.error(throwable);
                                            }
                                            return Observable.just(tProgress);
                                        }
                                    });
                            return fetching.concatWith(stash);
                        }
                    });
            observables.add(next);
        } else {
            // Default scenario, return depending on stash state
            Observable<Progress<T>> next = bp.stashRunner.getState()
                    .concatMap(new Func1<StashState<T>, Observable<? extends Progress<T>>>() {
                        @Override public Observable<? extends Progress<T>> call(StashState<T> stashState) {
                            Observable<Progress<T>> received
                                    = Observable.just(Progress.receivedFromStash(stashState.stashData));
                            if (stashState.kind == StashState.Kind.Stash) {
                                Observable<Progress<T>> source
                                        = peekSourceRequestWithProgress(p, stashState.stashEntry, bp.stashRunner);
                                if (source != null) {
                                    return Observable.concat(received, source);
                                }
                                return received;
                            }
                            Observable<Progress<T>> source = getSourceRequestWithProgress(p, bp.stashRunner);
                            if (stashState.kind == StashState.Kind.Source) {
                                return source;
                            }
                            return Observable.concat(received, source);
                        }
                    });
            observables.add(fetching);
            observables.add(next);
        }
        observables.add(complete);
        return Observable.concat(Observable.from(observables));
    }

    @Nullable public Stashable<T> getStashable(@NonNull P p) {
        return stashableProcessor.getStashable(p);
    }

    @NonNull private StashPolicy getStashPolicy(@NonNull P p) {
        if (p instanceof StashableParams<?>) {
            return ((StashableParams<?>) p).getStashPolicy();
        }
        return StashPolicy.SOURCE_ONLY_NO_STASH;
    }

    @NonNull private Blueprint<T> getBlueprint(@NonNull P p) {
        final StashPolicy stashPolicy = getStashPolicy(p);
        if (stashPolicy == StashPolicy.SOURCE_ONLY_NO_STASH) {
            return Blueprint.create(Blueprint.Steps.SourceOnly);
        }

        final Blueprint.Steps steps;
        final StashRunner<T> runner = StashRunner.create(getStashable(p), stashPolicy);
        if (stashPolicy == StashPolicy.STASH_ONLY_NO_SOURCE) {
            if (runner == null) {
                steps = Blueprint.Steps.None;
            } else {
                steps = Blueprint.Steps.StashOnly;
            }
        } else if (runner == null) {
            steps = Blueprint.Steps.SourceOnly;
        } else {
            if (stashPolicy == StashPolicy.SOURCE) {
                steps = Blueprint.Steps.SourceAndSaveOnly;
            } else if (stashPolicy == StashPolicy.SOURCE_UNLESS_ERROR) {
                steps = Blueprint.Steps.SourceAndSave_OnErrorStash;
            } else {
                steps = Blueprint.Steps.StashState;
            }
        }
        return Blueprint.create(runner, steps);
    }

    @NonNull private Observable<T> getSourceRequest(@NonNull P p) {
        return getSourceRequest(p, null);
    }

    @NonNull private Observable<Progress<T>> getSourceRequestWithProgress(@NonNull P p) {
        return getSourceRequestWithProgress(p, null);
    }

    @NonNull private Observable<T> getSourceRequest(@NonNull P p, @Nullable StashRunner<T> stashRunner) {
        return map(getSourceRequestWithProgress(p, stashRunner));
    }

    @NonNull
    private Observable<Progress<T>> getSourceRequestWithProgress(@NonNull P p, @Nullable StashRunner<T> stashRunner) {
        if (stashRunner != null) {
            return sourceProcessor.getSourceRequest(p, stashRunner.saveFunctionWithProgress());
        }
        return sourceProcessor.getSourceRequest(p);
    }

    @Nullable
    private Observable<T> peekSourceRequest(@NonNull P p, @Nullable Entry<T> entry,
            @NonNull StashRunner<T> stashRunner) {
        Observable<Progress<T>> request = peekSourceRequestWithProgress(p, entry, stashRunner);
        if (request != null) {
            return map(request);
        }
        return null;
    }

    @Nullable
    private Observable<Progress<T>> peekSourceRequestWithProgress(@NonNull P p, @Nullable Entry<T> entry,
            @NonNull StashRunner<T> stashRunner) {
        if (sourceProcessor instanceof OpenEndedProcessor) {
            return ((OpenEndedProcessor<T, P>) sourceProcessor).scheduleNext(p, entry,
                    stashRunner.saveFunctionWithProgress());
        }
        return sourceProcessor.peekSourceRequest(p);
    }

    @NonNull private Observable<T> map(@NonNull Observable<Progress<T>> observable) {
        return observable.filter(new Func1<Progress<T>, Boolean>() {
            @Override public Boolean call(Progress<T> tProgress) {
                return tProgress.getStatus() == Progress.Status.RECEIVED_FROM_SOURCE;
            }
        }).map(new Func1<Progress<T>, T>() {
            @Override public T call(Progress<T> tProgress) {
                return tProgress.getData();
            }
        });
    }

    private static final class StashableProcessorImpl<T, P extends Params> implements StashableProcessor<T, P> {
        private final Source<T, P> source;

        private StashableProcessorImpl(Source<T, P> source) {
            this.source = source;
        }

        @Nullable @Override public Stashable<T> getStashable(@NonNull P p) {
            if (p instanceof StashableParams<?>) {
                return getStashable((StashableParams<?>) p);
            }
            return null;
        }

        @Nullable private <SP extends StashableParams<?>> Stashable<T> getStashable(SP sp) {
            if (source instanceof StashableSource<?, ?>) {
                return ((StashableSource<T, SP>) source).getStashable(sp);
            }
            return null;
        }
    }
}
