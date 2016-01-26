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

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import stash.Params;
import stash.Source;
import stash.SourceModuleBuilder;
import stash.sources.builder.SourceHandlerModule;

@stash.annotations.SourceModule(Source.class)
public final class SourceSimpleModule<T, P extends Params> implements SourceModuleBuilder {
    private final SourceModule<T, P> module = new SourceModule<T, P>();

    public SourceSimpleModule<T, P> source(@NonNull Action1<Subscriber<? super T>> source) {
        return sourceInternal(this, source);
    }

    public SourceSimpleModule<T, P> source(@NonNull Func0<T> source) {
        return sourceInternal(this, source);
    }

    public SourceSimpleModule<T, P> source(@NonNull final Observable<T> source) {
        return sourceInternal(this, source);
    }

    @NonNull @Override public SourceHandlerModule build() {
        return module.build();
    }

    private static <T, P extends Params> SourceSimpleModule<T, P> sourceInternal(
            @NonNull SourceSimpleModule<T, P> module, @NonNull final Action1<Subscriber<? super T>> source) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.source(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                source.call(subscriber);
            }
        });
        return module;
    }

    private static <T, P extends Params> SourceSimpleModule<T, P> sourceInternal(
            @NonNull SourceSimpleModule<T, P> module, @NonNull final Func0<T> source) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.source(new Func1<P, T>() {
            @Override public T call(P p) {
                return source.call();
            }
        });
        return module;
    }

    private static <T, P extends Params> SourceSimpleModule<T, P> sourceInternal(
            @NonNull SourceSimpleModule<T, P> module, @NonNull final Observable<T> source) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.source(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, final Subscriber<? super T> subscriber) {
                source.unsafeSubscribe(new Subscriber<T>(subscriber) {
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
        });
        return module;
    }
}
