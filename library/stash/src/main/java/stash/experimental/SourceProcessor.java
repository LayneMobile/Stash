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

import com.google.common.collect.ImmutableList;

import rx.Observable;
import rx.Subscriber;
import stash.Params;
import stash.Request;
import stash.Source;

public class SourceProcessor<T, P extends Params> implements RequestProcessor<T, P> {
    private final Source<T, P> source;

    public SourceProcessor(Source<T, P> source) {
        this.source = source;
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
