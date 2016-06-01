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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Subscriber;
import stash.Api;
import stash.Params;
import stash.Source;
import stash.exceptions.NetworkUnavailableException;
import stash.internal.StashLog;
import stash.params.NetworkParams;
import stash.sources.NetworkSource;
import stash.sources.PreparableSource;
import stash.util.NetworkChecker;


class DefaultSourceProcessor<T, P extends Params> {
    private static final String TAG = DefaultSourceProcessor.class.getSimpleName();

    private final Source<T, P> source;

    protected DefaultSourceProcessor(@NonNull Source<T, P> source) {
        this.source = source;
    }

    @NonNull
    static <T, P extends Params> DefaultSourceProcessor<T, P> create(@NonNull Source<T, P> source) {
        return new DefaultSourceProcessor<T, P>(source);
    }

    @NonNull
    public Observable<T> getSourceRequest(@NonNull P p) {
        Observable<T> request = Observable.create(new OnSubscribeImpl(p));
        if (source instanceof PreparableSource) {
            return ((PreparableSource<T, P>) source).prepareSourceRequest(request, p);
        }
        return request;
    }

    @Nullable public Observable<T> peekSourceRequest(@NonNull P p) {
        return null;
    }

    @Nullable private NetworkChecker getNetworkChecker() {
        if (source instanceof NetworkSource) {
            return ((NetworkSource) source).getNetworkChecker();
        }
        return null;
    }

    /*

    steps :

    Source {
        void call(P params, Subscriber<? super T> subscriber);

        -> Observable<T> observable = from(source.call(p, subscriber));

        Internal {
            Observable<T> request(P p);
        }
    }

    NetworkSource extends Source {
        NetworkChecker networkChecker();

        Internal {
            Observable<T> request (P p, NetworkSource s, Observable<T> request) {
                if (!hasNetwork(s.networkChecker())) {
                    return Observable.error();
                } else {
                    return request;
                }
            }
        }
    }

    PreparableSource extends Source {
        Observable<T> prepareSource(Observable<T> source);


    }

    Request<T> request = processor.request(P params);

     */

    private final class OnSubscribeImpl implements Observable.OnSubscribe<T> {
        private final P p;

        private OnSubscribeImpl(P p) {
            this.p = p;
        }

        @Override public void call(final Subscriber<? super T> subscriber) {
            if (subscriber.isUnsubscribed()) { return; }
            if (source instanceof NetworkSource || p instanceof NetworkParams) {
                StashLog.d(TAG, "checking network connection");
                Context context = Api.getContext(p);
                if (context == null) {
                    StashLog.w(TAG, "context is null. unable to check network connection");
                } else {
                    NetworkChecker networkChecker = getNetworkChecker();
                    if (networkChecker == null) {
                        networkChecker = NetworkChecker.DEFAULT;
                    }
                    if (!networkChecker.isNetworkAvailable(context)) {
                        StashLog.i(TAG, "not running request. no network");
                        subscriber.onError(new NetworkUnavailableException("no network"));
                        return;
                    }
                }
            }
            source.call(p, new Subscriber<T>(subscriber) {
                @Override public void onCompleted() {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                }

                @Override public void onError(Throwable e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }

                @Override public void onNext(T t) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(t);
                    }
                }
            });
        }
    }
}
