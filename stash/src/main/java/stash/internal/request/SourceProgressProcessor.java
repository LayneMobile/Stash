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
import stash.Params;
import stash.Progress;
import stash.Source;
import stash.SourceProcessor;

class SourceProgressProcessor<T, P extends Params> {
    private final Observable<Progress<T>> callingSource = Observable.just(Progress.<T>callingSource());
    private final Func1<T, Progress<T>> map = new Func1<T, Progress<T>>() {
        @Override public Progress<T> call(T t) {
            return Progress.receivedFromSource(t);
        }
    };
    private final SourceProcessor<T, P> sourceProcessor;

    protected SourceProgressProcessor(@NonNull Source<T, P> source) {
        this.sourceProcessor = new SourceProcessor.Builder<T, P>()
                .setSource(source)
                .build();
    }

    protected SourceProgressProcessor(@NonNull SourceProcessor<T, P> sourceProcessor) {
        this.sourceProcessor = sourceProcessor;
    }

    @NonNull
    public static <T, P extends Params> SourceProgressProcessor<T, P> create(@NonNull Source<T, P> source) {
        return new SourceProgressProcessor<T, P>(source);
    }

    @NonNull public static <T, P extends Params> SourceProgressProcessor<T, P> create(
            @NonNull SourceProcessor<T, P> sourceProcessor) {
        return new SourceProgressProcessor<T, P>(sourceProcessor);
    }

    @NonNull public Observable<Progress<T>> getSourceRequest(@NonNull P p) {
        return map(sourceProcessor.getSourceRequest(p));
    }

    @NonNull
    public Observable<Progress<T>> getSourceRequest(@NonNull P p, @NonNull ConcatFunction<T> concatFunction) {
        return map(sourceProcessor.getSourceRequest(p), concatFunction);
    }

    @Nullable public Observable<Progress<T>> peekSourceRequest(@NonNull P p) {
        Observable<T> request = sourceProcessor.peekSourceRequest(p);
        if (request != null) {
            return map(request);
        }
        return null;
    }

    private Observable<Progress<T>> map(@NonNull Observable<T> observable) {
        return decorate(observable.map(map));
    }

    private Observable<Progress<T>> map(@NonNull Observable<T> observable, @NonNull ConcatFunction<T> concatFunction) {
        return decorate(observable.map(map)
                .concatMap(concatFunction));
    }

    private Observable<Progress<T>> decorate(@NonNull Observable<Progress<T>> observable) {
        return Observable.concat(callingSource, observable);
    }

    interface ConcatFunction<T> extends Func1<Progress<T>, Observable<? extends Progress<T>>> {}
}
