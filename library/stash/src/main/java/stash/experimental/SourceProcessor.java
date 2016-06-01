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

package stash.experimental;

import android.content.Context;

import com.google.common.collect.ImmutableList;

import rx.Observable;
import rx.Subscriber;
import stash.Api;
import stash.Params;
import stash.Request;
import stash.Source;
import stash.exceptions.NetworkUnavailableException;
import stash.internal.StashLog;
import stash.sources.NetworkSource;
import stash.util.NetworkChecker;

public class SourceProcessor<T, P extends Params> implements RequestProcessor<T, P> {
    private final Source<T, P> source;

    public SourceProcessor(Source<T, P> source) {
        this.source = source;
    }

    static class Intercept<T, P extends Params> implements Interceptor<T, P> {
        private final Source<T, P> source;

        public Intercept(Source<T, P> source) {
            this.source = source;
        }

        @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
            P params = chain.params();
            return null;
        }
    }

    public static class Transformer<T, P extends Params> implements Processor.Transformer<P, Source<T, P>, RequestProcessor<T, P>> {
        @Override public RequestProcessor<T, P> call(final Source<T, P> source) {
            return new SourceProcessor<>(source);
        }
    }

    static class NetworkSourceTransformer<T, P extends Params>
            implements Processor.Interceptor.Transformer<P, NetworkSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        private static final String TAG = NetworkSourceTransformer.class.getSimpleName();

        @Override public Interceptor<T, P> call(final NetworkSource<T, P> source) {
            return new Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    P p = chain.params();
                    StashLog.d(TAG, "checking network connection");
                    Context context = Api.getContext(p);
                    if (context == null) {
                        StashLog.w(TAG, "context is null. unable to check network connection");
                    } else {
                        NetworkChecker networkChecker = source.getNetworkChecker();
                        if (networkChecker == null) {
                            networkChecker = NetworkChecker.DEFAULT;
                        }
                        if (!networkChecker.isNetworkAvailable(context)) {
                            StashLog.i(TAG, "not running request. no network");
                            return Request.error(new NetworkUnavailableException("no network"));
                        }
                    }
                    return chain.proceed(p);
                }
            };
        }
    }

    @Override public Request<T> call(P p) {
        return Request.create(new OnSubscribeImpl(p));
    }

    private final class OnSubscribeImpl implements Observable.OnSubscribe<T> {
        private final P p;

        private OnSubscribeImpl(P p) {
            this.p = p;
        }

        @Override public void call(final Subscriber<? super T> subscriber) {
            if (subscriber.isUnsubscribed()) { return; }
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

    static class InterceptorChain<T, P extends Params> implements RequestProcessor<T, P> {
        private final SourceProcessor<T, P> processor;
        private final ImmutableList<Interceptor<T, P>> interceptors;

        public InterceptorChain(SourceProcessor<T, P> processor, ImmutableList<Interceptor<T, P>> interceptors) {
            this.processor = processor;
            this.interceptors = interceptors;
        }

        @Override public Request<T> call(P p) {
            return new Child(0, p)
                    .proceed(p);
        }

        private final class Child implements RequestProcessor.Interceptor.Chain<T, P> {
            private final int index;
            private final P params;

            private Child(int index, P params) {
                this.index = index;
                this.params = params;
            }

            @Override public P params() {
                return params;
            }

            @Override public Request<T> proceed(P p) {
                if (index < interceptors.size()) {
                    Child chain = new Child(index + 1, p);
                    return interceptors.get(index).intercept(chain);
                }
                return processor.call(p);
            }
        }
    }
}
