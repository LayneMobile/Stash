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
