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

import java.util.concurrent.atomic.AtomicReference;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import stash.internal.StashLog;

final class ReplayLatestOperator<T> implements Observable.Operator<T, Observable<T>> {
    private static final String TAG = ReplayLatestOperator.class.getSimpleName();
    private static final long MIN_POST_DIFF_MILLIS = 25;

    private volatile long lastUpdatedTime;
    private final Latest<NotificationNode<T>> latest;

    ReplayLatestOperator(Latest<NotificationNode<T>> latest) {
        this.latest = latest;
    }

    @Override public Subscriber<? super Observable<T>> call(final Subscriber<? super T> subscriber) {
        StashLog.d(TAG, "onSubscribe replay latest");
        return new ReplaySubscriber(subscriber);
    }

    private synchronized boolean postLatest(Subscriber<? super T> subscriber,
            AtomicReference<NotificationNode<T>> lastUpdated,
            boolean subscribedToSource) {
        final NotificationNode<T> latest = this.latest.get();
        final NotificationNode<T> current = lastUpdated.get();
        if (latest == current) {
            StashLog.d(TAG, "already current");
            return false;
        } else if (!lastUpdated.compareAndSet(current, latest)) {
            StashLog.w(TAG, "current was updated :(");
            return false;
        }

        boolean finished = false;
        final Notification<T> notification = getNotification(latest);
        if (notification != null) {
            if (latest.prev != null && notification.getKind() != Notification.Kind.OnNext) {
                long currentTime = System.currentTimeMillis();
                long lastUpdatedTime = this.lastUpdatedTime;
                long diff = currentTime - lastUpdatedTime;
                if (diff < MIN_POST_DIFF_MILLIS) {
                    long sleep = MIN_POST_DIFF_MILLIS - diff;
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
            switch (notification.getKind()) {
                case OnCompleted:
                    StashLog.d(TAG, "latest onNext and onComplete. finished.%s",
                            subscribedToSource ? "" : " Not subscribing to source");
                    for (Notification<T> prev : latest.prevList(current)) {
                        // should only be data, but verify
                        if (prev.getKind() == Notification.Kind.OnNext) {
                            subscriber.onNext(prev.getValue());
                        }
                    }
                    subscriber.onCompleted();
                    finished = true;
                    break;
                case OnError:
                    StashLog.d(TAG, "latest onNext and onError. finished.%s",
                            subscribedToSource ? "" : " Not subscribing to source");
                    for (Notification<T> prev : latest.prevList(current)) {
                        // should only be data, but verify
                        if (prev.getKind() == Notification.Kind.OnNext) {
                            subscriber.onNext(prev.getValue());
                        }
                    }
                    subscriber.onError(notification.getThrowable());
                    finished = true;
                    break;
                case OnNext:
                    StashLog.d(TAG, "latest onNext. not finished.%s",
                            subscribedToSource ? "" : " possibly subscribing to source");
                    for (Notification<T> prev : latest.prevList(current)) {
                        // should only be data, but verify
                        if (prev.getKind() == Notification.Kind.OnNext) {
                            subscriber.onNext(prev.getValue());
                        }
                    }
                    subscriber.onNext(notification.getValue());
                    break;
            }
            this.lastUpdatedTime = System.currentTimeMillis();
        }
        return finished;
    }

    private Notification<T> getNotification(NotificationNode<T> node) {
        return node == null ? null : node.current;
    }

    interface Latest<T> {
        T get();
    }

    private class ReplaySubscriber extends Subscriber<Observable<T>> {
        private final AtomicReference<NotificationNode<T>> current = new AtomicReference<NotificationNode<T>>();
        private final Subscriber<? super T> subscriber;

        public ReplaySubscriber(Subscriber<? super T> subscriber) {
            super(subscriber);
            this.subscriber = subscriber;
        }

        @Override public void onCompleted() {
            // we only expect one
        }

        @Override public void onError(Throwable e) {
            // shouldn't ever happen
            subscriber.onError(e);
        }

        @Override public void onNext(Observable<T> observable) {
            // If we've already completed, no need to subscribe to the source
            if (postLatest(subscriber, current, false)) {
                StashLog.d(TAG, "already complete. not subscribing to the source");
                return;
            }

            // subscribe since the source is not finished
            StashLog.d(TAG, "subscribing to source");
            observable.unsafeSubscribe(new SourceSubscriber(subscriber, current));
        }
    }

    final class SourceSubscriber extends Subscriber<T> {
        private final AtomicReference<NotificationNode<T>> current;
        private final Subscriber<? super T> subscriber;

        SourceSubscriber(Subscriber<? super T> subscriber) {
            this(subscriber, new AtomicReference<NotificationNode<T>>());
        }

        SourceSubscriber(Subscriber<? super T> subscriber, AtomicReference<NotificationNode<T>> current) {
            super(subscriber);
            this.current = current;
            this.subscriber = subscriber;
        }

        @Override public void onCompleted() {
            // do this to ensure nothing was missed
            if (!postLatest(subscriber, current, true)) {
                subscriber.onCompleted();
            }
        }

        @Override public void onError(Throwable e) {
            // do this to ensure nothing was missed
            if (!postLatest(subscriber, current, true)) {
                subscriber.onError(e);
            }
        }

        @Override public void onNext(T t) {
            StashLog.d(TAG, "onNext from source");
            postLatest(subscriber, current, true);
        }

        void syncLatest() {
            postLatest(subscriber, current, true);
        }
    }
}
