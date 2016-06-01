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

package stash.sources.modules;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func1;
import stash.Params;
import stash.Source;
import stash.SourceModuleBuilder;
import stash.sources.builder.SourceHandler;
import stash.sources.builder.SourceMethodHandler;
import stash.types.MethodResult;

@stash.annotations.SourceModule(Source.class)
public final class SourceModule<T, P extends Params> implements SourceModuleBuilder {
    private Action2<P, Subscriber<? super T>> source;

    public SourceModule<T, P> source(@NonNull Action2<P, Subscriber<? super T>> source) {
        this.source = source;
        return this;
    }

    public SourceModule<T, P> source(@NonNull Func1<P, T> source) {
        return sourceInternal(this, source);
    }

    @NonNull @Override public SourceHandler build() {
        if (source == null) {
            throw new IllegalStateException("source must be set");
        }
        return new SourceHandler.Builder(Source.class)
                .handle("call", new Handler<T, P>(source))
                .build();
    }

    private static <T, P extends Params> SourceModule<T, P> sourceInternal(@NonNull SourceModule<T, P> module,
            @NonNull final Func1<P, T> source) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        return module.source(new Action2<P, Subscriber<? super T>>() {
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
        });
    }

    private static final class Handler<T, P extends Params> implements SourceMethodHandler {
        private final Action2<P, Subscriber<? super T>> source;

        private Handler(Action2<P, Subscriber<? super T>> source) {
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
}
