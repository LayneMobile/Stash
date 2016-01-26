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

package stash.retrofit;

import android.support.annotation.NonNull;

import retrofit.RestAdapter;
import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.Params;
import stash.SourceModulesBuilder;
import stash.sources.modules.NetworkSourceModule;
import stash.sources.modules.SourceModule;
import stash.sources.builder.SourceHandlerModule;
import stash.util.NetworkChecker;

@stash.annotations.SourceModule(value = RetrofitSource.class, simple = false)
public final class RetrofitSourceModule<T, P extends Params, S> extends SourceModulesBuilder {
    private final Retrofittable.Builder<S> retrofittable = new Retrofittable.Builder<S>();
    private SourceBuilder<T, P, S> builder;

    @NonNull public RetrofitSourceModule<T, P, S> serviceType(@NonNull Class<S> serviceType) {
        retrofittable.setServiceType(serviceType);
        return this;
    }

    @NonNull public RetrofitSourceModule<T, P, S> restAdapter(@NonNull RestAdapter restAdapter) {
        retrofittable.setRestAdapter(restAdapter);
        return this;
    }

    @NonNull public RetrofitSourceModule<T, P, S> restAdapter(@NonNull Func0<RestAdapter> restAdapter) {
        retrofittable.setRestAdapter(restAdapter);
        return this;
    }

    @NonNull public RetrofitSourceModule<T, P, S> networkChecker(NetworkChecker networkChecker) {
        getBuilder().networkChecker(networkChecker);
        return this;
    }

    @NonNull public RetrofitSourceModule<T, P, S> source(@NonNull final Action3<S, P, Subscriber<? super T>> action) {
        getBuilder().source(action);
        return this;
    }

    @NonNull public RetrofitSourceModule<T, P, S> source(@NonNull final Func2<S, P, T> func) {
        getBuilder().source(func);
        return this;
    }

    @NonNull @Override public SourceHandlerModule[] buildModules() {
        return getBuilder().buildModules();
    }

    private synchronized SourceBuilder<T, P, S> getBuilder() {
        if (builder == null) {
            builder = new SourceBuilder<T, P, S>(retrofittable.build());
        }
        return builder;
    }

    private static final class SourceBuilder<T, P extends Params, S> {
        private final SourceModule<T, P> sourceModule = new SourceModule<T, P>();
        private final NetworkSourceModule networkModule = new NetworkSourceModule();
        private final Retrofittable<S> retrofittable;

        private SourceBuilder(Retrofittable<S> retrofittable) {
            if (retrofittable == null) {
                throw new IllegalStateException("retrofittable must not be null");
            }
            this.retrofittable = retrofittable;
        }

        private void networkChecker(NetworkChecker networkChecker) {
            networkModule.requiresNetwork(networkChecker);
        }

        private void source(@NonNull Action3<S, P, Subscriber<? super T>> action) {
            sourceInternal(sourceModule, retrofittable, action);
        }

        private void source(@NonNull Func2<S, P, T> func) {
            sourceInternal(sourceModule, retrofittable, func);
        }

        @NonNull private SourceHandlerModule[] buildModules() {
            return new SourceHandlerModule[]{
                    sourceModule.build(),
                    networkModule.build()
            };
        }

        private static <T, P extends Params, S> void sourceInternal(SourceModule<T, P> sourceModule,
                final Retrofittable<S> retrofittable,
                final Action3<S, P, Subscriber<? super T>> action) {
            // Create anonymous inner class in static context to avoid holding Module instance in memory
            sourceModule.source(new Action2<P, Subscriber<? super T>>() {
                @Override public void call(P p, Subscriber<? super T> subscriber) {
                    action.call(retrofittable.getService(), p, subscriber);
                }
            });
        }

        private static <T, P extends Params, S> void sourceInternal(SourceModule<T, P> sourceModule,
                final Retrofittable<S> retrofittable,
                final Func2<S, P, T> func) {
            // Create anonymous inner class in static context to avoid holding Module instance in memory
            sourceModule.source(new Func1<P, T>() {
                @Override public T call(P p) {
                    return func.call(retrofittable.getService(), p);
                }
            });
        }
    }
}
