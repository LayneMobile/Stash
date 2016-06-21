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
import stash.sources.builder.SourceHandler;
import stash.util.NetworkChecker;

@stash.annotations.SourceModule(RetrofitSource.class)
public final class RetrofitSourceSimpleModule<T, P extends Params, S> extends SourceModulesBuilder {
    private final RetrofitSourceModule<T, P, S> module = new RetrofitSourceModule<T, P, S>();

    @NonNull public RetrofitSourceSimpleModule<T, P, S> serviceType(@NonNull Class<S> serviceType) {
        module.serviceType(serviceType);
        return this;
    }

    @NonNull public RetrofitSourceSimpleModule<T, P, S> restAdapter(@NonNull RestAdapter restAdapter) {
        module.restAdapter(restAdapter);
        return this;
    }

    @NonNull public RetrofitSourceSimpleModule<T, P, S> restAdapter(@NonNull Func0<RestAdapter> restAdapter) {
        module.restAdapter(restAdapter);
        return this;
    }

    @NonNull public RetrofitSourceSimpleModule<T, P, S> networkChecker(NetworkChecker networkChecker) {
        module.networkChecker(networkChecker);
        return this;
    }

    @NonNull
    public RetrofitSourceSimpleModule<T, P, S> source(@NonNull Action2<S, Subscriber<? super T>> action) {
        sourceInternal(this, action);
        return this;
    }

    @NonNull public RetrofitSourceSimpleModule<T, P, S> source(@NonNull Func1<S, T> func) {
        sourceInternal(this, func);
        return this;
    }

    @NonNull @Override public SourceHandler[] buildModules() {
        return module.buildModules();
    }

    private static <T, P extends Params, S> void sourceInternal(RetrofitSourceSimpleModule<T, P, S> module,
            final Action2<S, Subscriber<? super T>> action) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.source(new Action3<S, P, Subscriber<? super T>>() {
            @Override public void call(S s, P p, Subscriber<? super T> subscriber) {
                action.call(s, subscriber);
            }
        });
    }

    private static <T, P extends Params, S> void sourceInternal(RetrofitSourceSimpleModule<T, P, S> module,
            final Func1<S, T> func) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.source(new Func2<S, P, T>() {
            @Override public T call(S s, P p) {
                return func.call(s);
            }
        });
    }
}
