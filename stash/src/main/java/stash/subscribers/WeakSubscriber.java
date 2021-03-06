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

package stash.subscribers;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import rx.Observer;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observers.Subscribers;
import stash.annotations.Keep;

/**
 * Helper class that removes its reference to a subscriber after unsubscribe is called.
 *
 * @param <T>
 */
public final class WeakSubscriber<T> extends Subscriber<T> {
    private final Delegate<T> delegate;

    private WeakSubscriber(Subscriber<? super T> actual, Delegate<T> delegate) {
        super(delegate);
        this.delegate = delegate;
        add(new WeakSubscription(delegate, actual));
    }

    public static <T> WeakSubscriber<T> create(Action1<? super T> onNext) {
        return create(new ActionSubscriber<T>(onNext));
    }

    public static <T> WeakSubscriber<T> create(Action1<? super T> onNext, Action1<Throwable> onError) {
        return create(new ActionSubscriber<T>(onNext, onError));
    }

    public static <T> WeakSubscriber<T> create(Action1<? super T> onNext, Action1<Throwable> onError,
            Action0 onCompleted) {
        return create(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public static <T> WeakSubscriber<T> create(Observer<? super T> actual) {
        return create(Subscribers.from(actual));
    }

    public static <T> WeakSubscriber<T> create(Subscriber<? super T> actual) {
        return new WeakSubscriber<T>(actual, new Delegate<T>());
    }

    @Override public void onNext(T t) {
        delegate.onNext(t);
    }

    @Override public void onError(Throwable e) {
        delegate.onError(e);
    }

    @Override public void onCompleted() {
        delegate.onCompleted();
    }

    @Override public void onStart() {
        delegate.onStart();
    }

    private static final class WeakSubscription implements Subscription {
        private static final AtomicIntegerFieldUpdater<WeakSubscription> UNSUBSCRIBED_UPDATER
                = AtomicIntegerFieldUpdater.newUpdater(WeakSubscription.class, "unsubscribed");

        private final Delegate<?> parent;
        @Keep private volatile int unsubscribed;

        private <T> WeakSubscription(@NonNull Delegate<T> parent, @NonNull Subscriber<? super T> child) {
            parent.actual = child;
            this.parent = parent;
        }

        @Override public void unsubscribe() {
            if (UNSUBSCRIBED_UPDATER.compareAndSet(this, 0, 1)) {
                parent.actual.unsubscribe();
                parent.actual = null;
            }
        }

        @Override public boolean isUnsubscribed() {
            return unsubscribed != 0;
        }
    }

    private static final class Delegate<T> extends Subscriber<T> {
        private volatile Subscriber<? super T> actual;

        @Override public void onNext(T t) {
            final Subscriber<? super T> actual = this.actual;
            if (actual != null) {
                actual.onNext(t);
            }
        }

        @Override public void onError(Throwable e) {
            final Subscriber<? super T> actual = this.actual;
            if (actual != null) {
                actual.onError(e);
            }
        }

        @Override public void onCompleted() {
            final Subscriber<? super T> actual = this.actual;
            if (actual != null) {
                actual.onCompleted();
            }
        }

        @Override public void setProducer(Producer producer) {
            Subscriber<? super T> actual = this.actual;
            if (actual != null) {
                actual.setProducer(producer);
            }
        }

        @Override public void onStart() {
            Subscriber<? super T> actual = this.actual;
            if (actual != null) {
                actual.onStart();
            }
        }
    }
}
