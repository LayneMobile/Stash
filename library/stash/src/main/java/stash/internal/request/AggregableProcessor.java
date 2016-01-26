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

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import stash.Aggregable;
import stash.Params;
import stash.Progress;
import stash.sources.AggregableSource;


class AggregableProcessor<T, P extends Params> extends SourceProgressProcessor<T, P> {
    private static final EmptyConcatFunction EMPTY_CONCAT_FUNCTION = new EmptyConcatFunction();

    private final Map<Object, Aggregate<Progress<T>>> aggregates = new HashMap<Object, Aggregate<Progress<T>>>(4);
    private final OnAggregateComplete onAggregateComplete = new OnAggregateComplete();
    private final AggregableSource<T, P> source;

    AggregableProcessor(@NonNull AggregableSource<T, P> source) {
        super(source);
        this.source = source;
    }

    @NonNull static <T, P extends Params> AggregableProcessor<T, P> create(
            @NonNull AggregableSource<T, P> source) {
        return new AggregableProcessor<T, P>(source);
    }

    @NonNull @Override public Observable<Progress<T>> getSourceRequest(@NonNull P p) {
        return getSourceRequest(p, AggregableProcessor.<T>emptyFunction());
    }

    @NonNull @Override
    public Observable<Progress<T>> getSourceRequest(@NonNull P p, @NonNull ConcatFunction<T> concatFunction) {
        final Object aggregateKey;
        final Aggregable aggregable = source.getAggregable(p);
        if (aggregable == null || (aggregateKey = aggregable.key()) == null) {
            return super.getSourceRequest(p, concatFunction);
        }

        Aggregate<Progress<T>> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregateKey);
        }
        if (aggregate == null || aggregate.isCompleted()) {
            Observable<Progress<T>> request = super.getSourceRequest(p, concatFunction);
            synchronized (aggregates) {
                aggregate = aggregates.get(aggregateKey);
                if (aggregate == null || aggregate.isCompleted()) {
                    aggregate = new Aggregate<Progress<T>>(aggregable, request, onAggregateComplete);
                    aggregates.put(aggregateKey, aggregate);
                }
            }
        }
        return aggregate.request;
    }

    @Nullable @Override public Observable<Progress<T>> peekSourceRequest(@NonNull P p) {
        final Object aggregateKey;
        final Aggregable aggregable = source.getAggregable(p);
        if (aggregable != null && (aggregateKey = aggregable.key()) != null) {
            final Aggregate<Progress<T>> aggregate;
            synchronized (aggregates) {
                aggregate = aggregates.get(aggregateKey);
            }
            if (aggregate != null && !aggregate.isCompleted() && !aggregate.isUnsubscribed()) {
                return aggregate.request;
            }
        }
        return null;
    }

    static <T> EmptyConcatFunction<T> emptyFunction() {
        return (EmptyConcatFunction<T>) EMPTY_CONCAT_FUNCTION;
    }

    private class OnAggregateComplete implements Action1<Aggregate<Progress<T>>> {
        @Override public void call(Aggregate<Progress<T>> progressAggregate) {
            synchronized (aggregates) {
                aggregates.remove(progressAggregate.aggregable.key());
            }
        }
    }

    private static final class EmptyConcatFunction<T> implements ConcatFunction<T> {
        @Override public Observable<? extends Progress<T>> call(Progress<T> tProgress) {
            return Observable.just(tProgress);
        }
    }
}
