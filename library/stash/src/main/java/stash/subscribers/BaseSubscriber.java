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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import rx.Subscriber;
import stash.annotations.Keep;
import stash.exceptions.StashBaseException;
import stash.exceptions.StashUnknownException;

public abstract class BaseSubscriber<T> extends NotifyingSubscriber<T> {
    private static final AtomicIntegerFieldUpdater<BaseSubscriber> FINISHED_UPDATOR
            = AtomicIntegerFieldUpdater.newUpdater(BaseSubscriber.class, "finished");

    @Keep private volatile int finished;
    private volatile boolean calledSuper;

    protected BaseSubscriber() {
        super();
    }

    protected BaseSubscriber(Subscriber<?> op) {
        super(op);
    }

    protected BaseSubscriber(Subscriber<?> subscriber, boolean shareSubscriptions) {
        super(subscriber, shareSubscriptions);
    }

    /**
     * Called when there is new data. May be called more than once depending on the observable implementation.
     *
     * @param t
     *         the data
     */
    @Override
    public abstract void onNext(T t);

    /**
     * Called when there is an error.
     *
     * @param e
     *         the wrapped exception that occurred
     */
    protected abstract void onError(StashBaseException e);

    /**
     * Called when either {@link #onCompleted()}, {@link #onError(Throwable)} or {@link #onUnsubscribe()} is called.
     * Will only be called once. Use this method to dismiss dialogs, etc.
     *
     * @param complete
     *         true if the observable completed, false if unsubscribe() was called before completing
     */
    protected void onFinished(boolean complete) {
        calledSuper = true;
        // allow subclass implementation
    }

    @Override
    public final void onError(Throwable e) {
        // handle
        try {
            onError(cast(e));
        } finally {
            if (FINISHED_UPDATOR.compareAndSet(this, 0, 1)) {
                callFinished(true);
            }
        }
    }

    @Override
    public final void onCompleted() {
        if (FINISHED_UPDATOR.compareAndSet(this, 0, 1)) {
            callFinished(true);
        }
    }

    @Override
    protected final void onUnsubscribe() {
        // Only call finished here if onCompleted or onError has not been called
        if (FINISHED_UPDATOR.compareAndSet(this, 0, 1)) {
            callFinished(false);
        }
    }

    private void callFinished(boolean completed) {
        calledSuper = false;
        onFinished(completed);
        if (!calledSuper) {
            throw new IllegalStateException("must call super in onFinished()");
        }
    }

    private static StashBaseException cast(Throwable e) {
        if (e instanceof StashBaseException) {
            return (StashBaseException) e;
        }
        return new StashUnknownException(e);
    }
}
