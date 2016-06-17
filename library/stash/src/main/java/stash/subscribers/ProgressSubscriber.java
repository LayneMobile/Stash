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

import rx.Subscriber;
import stash.Progress;
import stash.internal.StashLog;

public abstract class ProgressSubscriber<T> extends BaseSubscriber<Progress<T>> {
    private static final String TAG = ProgressSubscriber.class.getSimpleName();
    private Progress.Status status = Progress.Status.PENDING;

    protected ProgressSubscriber() {
        super();
    }

    protected ProgressSubscriber(Subscriber<?> op) {
        super(op);
    }

    protected ProgressSubscriber(Subscriber<?> subscriber, boolean shareSubscriptions) {
        super(subscriber, shareSubscriptions);
    }

    /**
     * Called when the request progress status is updated.
     *
     * @param status
     *         the current progress status
     */
    protected abstract void onNextStatus(Progress.Status status);

    /**
     * Called when the request has new data.
     *
     * @param t
     *         the data
     */
    protected abstract void onNextData(T t);

    @Override
    public final void onNext(Progress<T> progress) {
        final Progress.Status status;
        if (progress != null && (status = progress.getStatus()) != null) {
            this.status = status;
            StashLog.d(TAG, "onNextStatus: %s", status.name());
            onNextStatus(status);
            switch (status) {
                case RECEIVED_FROM_STASH:
                case RECEIVED_FROM_SOURCE:
                case SAVED_TO_STASH:
                    StashLog.d(TAG, "onNextData: %s", progress.getData());
                    onNextData(progress.getData());
                    break;
            }
        }
    }

    @NonNull
    public final Progress.Status getStatus() {
        return status;
    }
}
