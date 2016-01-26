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

import rx.Subscriber;
import stash.Progress;
import stash.annotations.Keep;
import stash.internal.StashLog;

public abstract class RefreshSubscriber<T> extends ProgressSubscriber<T> {
    private static final int PENDING = 0;
    private static final int REFRESHING = 1;
    private static final int FINISHED = 2;

    private static final String TAG = RefreshSubscriber.class.getSimpleName();
    private static final AtomicIntegerFieldUpdater<RefreshSubscriber> REFRESHING_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(RefreshSubscriber.class, "refreshing");

    private final RefreshListener refreshListener;
    @Keep private volatile int refreshing = PENDING;

    public RefreshSubscriber(@NonNull RefreshListener refreshListener) {
        super();
        this.refreshListener = refreshListener;
    }

    public RefreshSubscriber(Subscriber<?> op, @NonNull RefreshListener refreshListener) {
        super(op);
        this.refreshListener = refreshListener;
    }

    public RefreshSubscriber(Subscriber<?> subscriber, boolean shareSubscriptions,
            @NonNull RefreshListener refreshListener) {
        super(subscriber, shareSubscriptions);
        this.refreshListener = refreshListener;
    }

    @Override
    protected final void onNextStatus(Progress.Status status) {
        switch (status) {
            case CALLING_SOURCE:
                if (REFRESHING_UPDATER.compareAndSet(this, PENDING, REFRESHING)) {
                    refreshListener.onRefreshStarted();
                }
                break;
            case RECEIVED_FROM_SOURCE:
                if (REFRESHING_UPDATER.compareAndSet(this, REFRESHING, PENDING)) {
                    refreshListener.onRefreshCompleted();
                }
                break;
        }
    }

    @Override
    protected final void onFinished(boolean complete) {
        super.onFinished(complete);
        StashLog.d(TAG, "onFinished(complete = %s)", String.valueOf(complete));
        int status = REFRESHING_UPDATER.getAndSet(this, FINISHED);
        if (status == REFRESHING) {
            refreshListener.onRefreshCompleted();
        }
    }

    public interface RefreshListener {
        void onRefreshStarted();

        void onRefreshCompleted();
    }
}
