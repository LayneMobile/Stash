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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Notification;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import stash.Entry;
import stash.Params;
import stash.Progress;
import stash.aggregables.OpenEndedAggregable;
import stash.internal.StashLog;
import stash.schedulers.StashSchedulers;
import stash.sources.OpenEndedSource;

final class OpenEndedProcessor<T, P extends Params> extends AggregableProcessor<T, P> {
    private static final String TAG = OpenEndedProcessor.class.getSimpleName();

    private final HashMap<Object, Aggregate<T, P>> aggregates = new HashMap<Object, Aggregate<T, P>>();
    private final OpenEndedSource<T, P> source;
    private final OnNextSource<T, P> onNextSource = new OnNextSource<T, P>() {
        @NonNull @Override
        public Observable<Progress<T>> getSourceRequest(P p, ConcatFunction<T> concatFunction) {
            return getSuperRequest(p, concatFunction);
        }
    };
    private final Action1<OpenEndedOnSubscribe<T, P>> onCompleted = new Action1<OpenEndedOnSubscribe<T, P>>() {
        @Override public void call(OpenEndedOnSubscribe<T, P> onSubscribe) {
            synchronized (aggregates) {
                aggregates.remove(onSubscribe.aggregable.key());
            }
        }
    };

    private OpenEndedProcessor(@NonNull OpenEndedSource<T, P> source) {
        super(source);
        this.source = source;
    }

    @NonNull static <T, P extends Params> OpenEndedProcessor<T, P> create(@NonNull OpenEndedSource<T, P> source) {
        return new OpenEndedProcessor<T, P>(source);
    }

    @NonNull @Override public Observable<Progress<T>> getSourceRequest(@NonNull P p) {
        return getSourceRequest(p, OpenEndedProcessor.<T>emptyFunction());
    }

    @NonNull @Override
    public Observable<Progress<T>> getSourceRequest(@NonNull P p, @NonNull ConcatFunction<T> concatFunction) {
        final Object aggregableKey;
        final OpenEndedAggregable<T> aggregable = source.getAggregable(p);
        if (aggregable == null || (aggregableKey = aggregable.key()) == null) {
            return super.getSourceRequest(p, concatFunction);
        }

        Aggregate<T, P> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregableKey);
            if (aggregate == null) {
                aggregate = Aggregate.create(aggregable, p, concatFunction, onCompleted, onNextSource);
                aggregates.put(aggregableKey, aggregate);
            }
        }
        aggregate.updateSource(super.getSourceRequest(p, concatFunction));
        return aggregate.request;
    }

    @Nullable @Override public Observable<Progress<T>> peekSourceRequest(@NonNull P p) {
        final Object aggregableKey;
        final OpenEndedAggregable<T> aggregable = source.getAggregable(p);
        if (aggregable == null || (aggregableKey = aggregable.key()) == null) {
            return super.peekSourceRequest(p);
        }

        Aggregate<T, P> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregableKey);
            if (aggregate == null) {
                aggregate = Aggregate.create(aggregable, p, OpenEndedProcessor.<T>emptyFunction(), onCompleted,
                        onNextSource);
                aggregates.put(aggregableKey, aggregate);
            }
        }
        aggregate.updateSource(super.peekSourceRequest(p));
        return aggregate.request;
    }

    @Nullable Observable<Progress<T>> scheduleNext(@NonNull P p, @Nullable Entry<T> entry,
            @NonNull ConcatFunction<T> concatFunction) {
        final Object aggregableKey;
        final OpenEndedAggregable<T> aggregable = source.getAggregable(p);
        if (aggregable == null || (aggregableKey = aggregable.key()) == null) {
            return super.peekSourceRequest(p);
        }

        Aggregate<T, P> aggregate;
        synchronized (aggregates) {
            aggregate = aggregates.get(aggregableKey);
            if (aggregate == null) {
                aggregate = Aggregate.create(aggregable, p, concatFunction, onCompleted, onNextSource);
                aggregates.put(aggregableKey, aggregate);
            }
        }
        Observable<Progress<T>> source = super.peekSourceRequest(p);
        if (source != null) {
            aggregate.updateSource(source);
        } else {
            long delay = aggregable.delayUntilNextRefresh(entry);
            aggregate.scheduleNext(delay);
        }
        return aggregate.request;
    }

    @NonNull
    private Observable<Progress<T>> getSuperRequest(P p, ConcatFunction<T> concatFunction) {
        return super.getSourceRequest(p, concatFunction);
    }

    private static class Aggregate<T, P extends Params> {
        private final OpenEndedOnSubscribe<T, P> onSubscribe;
        private final Observable<Progress<T>> request;

        private Aggregate(OpenEndedOnSubscribe<T, P> onSubscribe) {
            this.onSubscribe = onSubscribe;
            this.request = Observable.create(onSubscribe);
        }

        private static <T, P extends Params> Aggregate<T, P> create(OpenEndedAggregable<T> aggregable,
                P p, ConcatFunction<T> concatFunction,
                Action1<OpenEndedOnSubscribe<T, P>> onCompleted,
                OnNextSource<T, P> onNextSource) {
            StashLog.d(TAG, "creating OpenEndedAggregate");
            OpenEndedOnSubscribe<T, P> onSubscribe
                    = new OpenEndedOnSubscribe<T, P>(aggregable, p, concatFunction, onCompleted, onNextSource);
            return new Aggregate<T, P>(onSubscribe);
        }

        private void updateSource(Observable<Progress<T>> source) {
            onSubscribe.updateSource(source);
        }

        private void scheduleNext(long delay) {
            onSubscribe.scheduleNext(delay);
        }
    }

    private static class OpenEndedOnSubscribe<T, P extends Params> implements Observable.OnSubscribe<Progress<T>> {
        private final OpenEndedAggregable<T> aggregable;
        private final P p;
        private final ConcatFunction<T> concatFunction;
        private final OnNextSource<T, P> onNextSource;
        private final Action1<OpenEndedOnSubscribe<T, P>> onComplete;
        private final AtomicReference<Observable<Progress<T>>> source
                = new AtomicReference<Observable<Progress<T>>>();
        private final CopyOnWriteArraySet<ReplayLatestOperator<Progress<T>>.SourceSubscriber> subscribers
                = new CopyOnWriteArraySet<ReplayLatestOperator<Progress<T>>.SourceSubscriber>();
        private final AtomicReference<SourceSubscriber> sourceSubscriber
                = new AtomicReference<SourceSubscriber>();
        private final ScheduleNext scheduleNext = new ScheduleNext();
        private final AtomicReference<Scheduler.Worker> scheduleWorker
                = new AtomicReference<Scheduler.Worker>();
        private final AtomicReference<NotificationNode<Progress<T>>> latest
                = new AtomicReference<NotificationNode<Progress<T>>>(new NotificationNode<Progress<T>>());
        private final ReplayLatestOperator<Progress<T>> replayOperator = new ReplayLatestOperator<Progress<T>>(
                new ReplayLatestOperator.Latest<NotificationNode<Progress<T>>>() {
                    @Override public NotificationNode<Progress<T>> get() {
                        return latest.get();
                    }
                });

        private OpenEndedOnSubscribe(OpenEndedAggregable<T> aggregable, P p, ConcatFunction<T> concatFunction,
                Action1<OpenEndedOnSubscribe<T, P>> onComplete, OnNextSource<T, P> onNextSource) {
            this.aggregable = aggregable;
            this.p = p;
            this.concatFunction = concatFunction;
            this.onComplete = onComplete;
            this.onNextSource = onNextSource;
        }

        @Override public void call(Subscriber<? super Progress<T>> subscriber) {
            final ReplayLatestOperator<Progress<T>>.SourceSubscriber wrap
                    = replayOperator.new SourceSubscriber(subscriber);
            subscriber.add(new Subscription() {
                final AtomicBoolean unsubscribed = new AtomicBoolean();

                @Override public void unsubscribe() {
                    if (unsubscribed.compareAndSet(false, true)) {
                        boolean complete;
                        SourceSubscriber sourceSubscriber = null;
                        synchronized (subscribers) {
                            OpenEndedOnSubscribe.this.subscribers.remove(wrap);
                            complete = OpenEndedOnSubscribe.this.subscribers.isEmpty();
                            if (complete) {
                                sourceSubscriber = OpenEndedOnSubscribe.this.sourceSubscriber.getAndSet(null);
                            }
                        }
                        if (sourceSubscriber != null) {
                            sourceSubscriber.unsubscribe();
                        }
                        if (complete) {
                            complete();
                        }
                    }
                }

                @Override public boolean isUnsubscribed() {
                    return unsubscribed.get();
                }
            });
            if (!subscriber.isUnsubscribed()) {
                synchronized (subscribers) {
                    subscribers.add(wrap);
                }
                subscribeToSource(source.get());
                wrap.syncLatest();
            }
        }

        private void complete() {
            StashLog.d(TAG, "complete");
            onComplete.call(this);
            Scheduler.Worker worker = scheduleWorker.getAndSet(null);
            if (worker != null) {
                worker.unsubscribe();
            }
        }

        private void updateSource(Observable<Progress<T>> source) {
            if (this.source.compareAndSet(null, source)) {
                StashLog.d(TAG, "source is null. Updating");
                subscribeToSource(source);
            } else {
                StashLog.d(TAG, "source isn't null, not updating");
            }
        }

        private void subscribeToSource(Observable<Progress<T>> source) {
            if (source == null) { return; }
            synchronized (subscribers) {
                if (subscribers.isEmpty()) {
                    StashLog.d(TAG, "no subscribers. Not yet subscribing to source");
                    return;
                }
            }
            StashLog.d(TAG, "Attempting to subscribe to source if not already");
            SourceSubscriber sourceSubscriber = this.sourceSubscriber.get();
            if (sourceSubscriber == null) {
                Scheduler.Worker worker = scheduleWorker.getAndSet(null);
                if (worker != null) {
                    worker.unsubscribe();
                }
                sourceSubscriber = new SourceSubscriber();
                if (this.sourceSubscriber.compareAndSet(null, sourceSubscriber)) {
                    StashLog.d(TAG, "subscribing to source");
                    sourceSubscriber.add(new Subscription() {
                        final AtomicBoolean unsubscribed = new AtomicBoolean();

                        @Override public void unsubscribe() {
                            if (unsubscribed.compareAndSet(false, true)) {
                                StashLog.d(TAG, "unsubscribed from source");
                                OpenEndedOnSubscribe.this.sourceSubscriber.set(null);
                            }
                        }

                        @Override public boolean isUnsubscribed() {
                            return unsubscribed.get();
                        }
                    });
                    source.subscribeOn(StashSchedulers.service())
                            .subscribe(sourceSubscriber);
                }
            }
        }

        private void updateLatest(Notification<Progress<T>> latest) {
            this.latest.set(this.latest.get().update(latest));
        }

        private void scheduleNext(long delay) {
            if (delay == -1) { return; }
            Scheduler.Worker worker = Schedulers.computation().createWorker();
            if (scheduleWorker.compareAndSet(null, worker)) {
                worker.schedule(scheduleNext, delay, TimeUnit.MILLISECONDS);
            } else {
                worker.unsubscribe();
            }
        }

        private class SourceSubscriber extends Subscriber<Progress<T>> {
            private volatile long delay = -1L;

            private SourceSubscriber() {}

            @Override public void onCompleted() {
                source.set(null);
                latest.set(new NotificationNode<Progress<T>>());
                StashLog.d(TAG, "onCompleted. subscribers: %d", subscribers.size());
                scheduleNext(delay);
            }

            @Override public void onError(Throwable e) {
                source.set(null);
                StashLog.e(TAG, "onError. subscribers: %d", subscribers.size());
                updateLatest(Notification.<Progress<T>>createOnError(e));
                for (Subscriber<? super Progress<T>> subscriber : subscribers) {
                    subscriber.onError(e);
                }
                latest.set(new NotificationNode<Progress<T>>());
            }

            @Override public void onNext(Progress<T> progress) {
                StashLog.d(TAG, "onNext %s. subscribers: %d", progress.getStatus(), subscribers.size());
                updateLatest(Notification.createOnNext(progress));
                for (Subscriber<? super Progress<T>> subscriber : subscribers) {
                    subscriber.onNext(progress);
                }

                switch (progress.getStatus()) {
                    case RECEIVED_FROM_SOURCE:
                        Entry<T> entry = new Entry.Builder<T>()
                                .setData(progress.getData())
                                .build();
                        delay = aggregable.delayUntilNextRefresh(entry);
                        break;
                }
            }
        }

        private class ScheduleNext implements Action0 {
            @Override public void call() {
                Observable<Progress<T>> source = onNextSource.getSourceRequest(p, concatFunction);
                updateSource(source);
            }
        }
    }

    private interface OnNextSource<T, P extends Params> {
        @NonNull Observable<Progress<T>> getSourceRequest(P p, ConcatFunction<T> concatFunction);
    }
}
