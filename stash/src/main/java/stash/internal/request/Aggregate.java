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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import stash.Aggregable;
import stash.annotations.Keep;
import stash.exceptions.SourceCancelledException;
import stash.internal.StashLog;
import stash.schedulers.StashSchedulers;

final class Aggregate<T> {
    private static final String TAG = Aggregate.class.getSimpleName();
    private static final AtomicIntegerFieldUpdater<Aggregate> SUBSCRIBED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(Aggregate.class, "subscribed");
    private static final AtomicIntegerFieldUpdater<Aggregate> UNSUBSCRIBED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(Aggregate.class, "unsubscribed");
    private static final AtomicIntegerFieldUpdater<Aggregate> COMPLETED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(Aggregate.class, "completed");

    final Aggregable aggregable;
    final Observable<T> request;
    private final PublishSubject<T> subject;
    private final Observable<T> source;
    private final Action1<Aggregate<T>> onComplete;
    private final AtomicReference<NotificationNode<T>> latest
            = new AtomicReference<NotificationNode<T>>(new NotificationNode<T>());
    private volatile Subscription subscription;
    @Keep private volatile int subscribed;
    @Keep private volatile int unsubscribed;
    @Keep private volatile int completed;

    Aggregate(Aggregable aggregable, Observable<T> request, Action1<Aggregate<T>> onComplete) {
        this.aggregable = aggregable;
        this.onComplete = onComplete;
        this.subject = PublishSubject.create();
        this.source = request.subscribeOn(StashSchedulers.service());
        this.request = subject.asObservable()
                .doOnSubscribe(new OnSubscribe())
                .nest()
                .lift(new ReplayLatestOperator<T>(new Latest<NotificationNode<T>>(latest)))
                .defaultIfEmpty(null);
    }

    boolean hasSubscribed() {
        return subscribed != 0;
    }

    void unsubscribe() {
        UNSUBSCRIBED_UPDATER.set(this, 1);
        if (SUBSCRIBED_UPDATER.compareAndSet(this, 1, 2)) {
            // We've subscribed
            Subscription subscription = this.subscription;
            if (subscription != null) {
                subscription.unsubscribe();
                Exception e = new SourceCancelledException();
                updateLatest(Notification.<T>createOnError(e));
                subject.onError(e);
            }
        }
        SUBSCRIBED_UPDATER.set(this, 2);
        complete();
    }

    boolean isUnsubscribed() {
        return hasSubscribed()
                // We only set subscription once, so it's save to read twice here
                && (unsubscribed != 0 || subscription == null || subscription.isUnsubscribed());
    }

    boolean isCompleted() {
        return completed != 0;
    }

    private void updateLatest(Notification<T> notification) {
        NotificationNode<T> prev = this.latest.get();
        NotificationNode<T> latest = prev.update(notification);
        while (!this.latest.compareAndSet(prev, latest)) {
            prev = this.latest.get();
            latest = prev.update(notification);
        }
    }

    private void scheduleComplete() {
        final int keepAliveSeconds = aggregable.keepAliveSeconds();
        if (keepAliveSeconds <= 0) {
            complete();
            return;
        }
        Schedulers.computation().createWorker().schedule(new Action0() {
            @Override public void call() {
                complete();
            }
        }, keepAliveSeconds, TimeUnit.SECONDS);
    }

    private void complete() {
        if (COMPLETED_UPDATER.compareAndSet(this, 0, 1)) {
            onComplete.call(this);
        }
    }

    private class OnSubscribe implements Action0 {
        @Override public void call() {
            // Subscribe to the source observable here
            final Aggregate<T> aggregate = Aggregate.this;
            if (SUBSCRIBED_UPDATER.compareAndSet(aggregate, 0, 1) && UNSUBSCRIBED_UPDATER.get(aggregate) == 0) {
                StashLog.d(TAG, "onSubscribe aggregate source");
                subscription = source.subscribe(new Subscriber<T>() {
                    @Override public void onCompleted() {
                        if (!isUnsubscribed()) {
                            updateLatest(Notification.<T>createOnCompleted());
                            subject.onCompleted();
                            scheduleComplete();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        if (!isUnsubscribed()) {
                            updateLatest(Notification.<T>createOnError(e));
                            subject.onError(e);
                            if (aggregable.keepAliveOnError()) {
                                scheduleComplete();
                            } else {
                                complete();
                            }
                        }
                    }

                    @Override public void onNext(T t) {
                        if (!isUnsubscribed()) {
                            updateLatest(Notification.createOnNext(t));
                            subject.onNext(t);
                        }
                    }
                });
                // handle thread-race condition
                if (UNSUBSCRIBED_UPDATER.get(aggregate) == 1 && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                    Exception e = new SourceCancelledException();
                    updateLatest(Notification.<T>createOnError(e));
                    subject.onError(e);
                }
            }
        }
    }

    private static final class Latest<T> implements ReplayLatestOperator.Latest<T> {
        private final AtomicReference<T> reference;

        private Latest(AtomicReference<T> reference) {
            this.reference = reference;
        }

        @Override public T get() {
            return reference.get();
        }
    }
}
