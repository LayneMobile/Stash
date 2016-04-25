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

package stash;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Function;

public interface Source<T, P extends Params> {
    void call(P p, Subscriber<? super T> subscriber);

    final class SourceTypeHandler<T, P extends Params> implements TypeHandlerModule<Source> {
        Action2<P, Subscriber<? super T>> action;

        @Override public TypeHandler<Source> build() {
            return null;
        }
    }

    /*
    Need to use annotations

    @ProxyType(Source.class) or @ProxyType("Source")
    class SourceProxy<T, P extends Params> {
        @ProxyMethod("call")
        Action2<P, Subscriber<? super T>> source;

        @ProxyMethod("get")
        Func0<T> value;

        @ProxyTransform("call")
        Action2<P, Subscriber<? super T>> source(final Action1<Subscriber<? super T>> source) {
            return new Action2<P, Subscriber<? super T>>() {
                @Override public void call(P p, Subscriber<? super T> subscriber) {
                    source.call(subscriber);
                }
            };
        }

        @ProxyTransform("call")
        Action2<P, Subscriber<? super T>> source(final Func1<P, T> source) {
            return new Action2<P, Subscriber<? super T>>() {
                @Override public void call(P p, Subscriber<? super T> subscriber) {
                    try {
                        T t = source.call(p);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(t);
                            subscriber.onCompleted();
                        }
                    } catch (Throwable e) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }
                    }
                }
            };
        }

        @ProxyTransform("get")
        Func0<T> value(final T t) {
            return new Func0<T>() {
                @Override public T call() {
                    return t;
                }
            };
        }
    }

    // Generated Interface
    interface Source<T, P extends Params> {
        void call(P p, Subscriber<? super T> subscriber);

        T get();
    }

    // Generated Invocation Handler
    final class SourceHandler<T, P extends Params> implements MethodHandler {
        private final Action2<P, Subscriber<? super T>> source;

        private SourceHandler(Action2<P, Subscriber<? super T>> source) {
            this.source = source;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2
                    && paramTypes[0].isAssignableFrom(Params.class)
                    && paramTypes[1].isAssignableFrom(Subscriber.class)) {
                source.call((P) args[0], (Subscriber<? super T>) args[1]);
                return true;
            }
            return false;
        }
    }

    // Generated Builder
    final class SourceModule<T, P extends Params> implements TypeModule<Source> {
        final ProxySource<T, P> proxy = new ProxySource<T, P>();

        Builder<T, P> source(Action2<P, Subscriber<? super T>> source) {
            proxy.source = source;
            return this;
        }

        Builder<T, P> source(Action1<Subscriber<? super T>> source) {
            proxy.source = proxy.source(source);
            return this;
        }

        Builder<T, P> source(final Func1<P, T> source) {
            proxy.source = proxy.source(source);
            return this;
        }

        Builder<T, P> value(Func0<T> value) {
            proxy.value = value;
            return this;
        }

        Builder<T, P> value(T t) {
            proxy.value = proxy.value(t);
            return this;
        }

       @Override TypeHandler<Source> build() {
            if (proxy.source == null) {
                throw new IllegalStateException("source must be set");
            }
            return new TypeHandler.Builder<Source>(Source.class)
                    .handle("call", new SourceHandler<T, P>(proxy.source))
                    .build();
        }
    }
    presents ->

    @GenerateBuilder(OtherProxySource.class)
    public interface ExtendedSource<T, P extends Params> implements Source<T, P> {}
    */

    interface ProxyInterface {
        String name();

        List<Method<?>> methods();

        interface Method<F extends Function> {
            String name();

            List<Transformer<F, ?>> transformers();
        }

        interface Transformer<R extends Function, T> {
            String name();

            R transform(T t);
        }
    }

    final class SourceProxy<T, P extends Params> implements ProxyInterface {
        @Override public String name() {
            return "Source";
        }

        @Override public List<Method<?>> methods() {
            List<Method<?>> methods = new ArrayList<>();
            methods.add(new Method<Action2<P, Subscriber<? super T>>>() {
                @Override public String name() {
                    return "call";
                }

                @Override public List<Transformer<Action2<P, Subscriber<? super T>>, ?>> transformers() {
                    List<Transformer<Action2<P, Subscriber<? super T>>, ?>> transformers = new ArrayList<Transformer<Action2<P, Subscriber<? super T>>, ?>>();
                    transformers.add(
                            new Transformer<Action2<P, Subscriber<? super T>>, Action1<Subscriber<? super T>>>() {
                                @Override public String name() {
                                    return "source";
                                }

                                @Override
                                public Action2<P, Subscriber<? super T>> transform(
                                        Action1<Subscriber<? super T>> subscriberAction1) {
                                    return new Action2<P, Subscriber<? super T>>() {
                                        @Override public void call(P p, Subscriber<? super T> subscriber) {
                                            subscriberAction1.call(subscriber);
                                        }
                                    };
                                }
                            });
                    return transformers;
                }
            });
            return methods;
        }
    }
}
