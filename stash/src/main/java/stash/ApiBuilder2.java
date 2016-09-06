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

import android.support.annotation.NonNull;

import com.laynemobile.proxy.ProxyBuilder;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Func2;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import stash.sources.AggregableSourceProxyHandlerBuilder;
import stash.sources.NetworkSourceProxyHandlerBuilder;
import stash.sources.PreparableSourceProxyHandlerBuilder;
import stash.util.NetworkChecker;

public final class ApiBuilder2<T, P extends Params> implements Builder<Api<T, P>> {
    @NonNull
    private final ProxyBuilder<Source<T, P>> builder = new ProxyBuilder<>(new TypeToken<Source<T, P>>() {});

    public ApiBuilder2() {}

    @NonNull
    public ApiBuilder2<T, P> source(Action2<P, Subscriber<? super T>> source) {
        return new SourceModuleBuilder()
                .source(source)
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> source(Func1<P, T> source) {
        return new SourceModuleBuilder()
                .source(source)
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> requiresNetwork() {
        return new NetworkSourceModuleBuilder()
                .requiresNetwork()
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> requiresNetwork(@NonNull NetworkChecker checker) {
        return new NetworkSourceModuleBuilder()
                .requiresNetwork(checker)
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> aggregate(Func1<P, Aggregable> action) {
        return new AggregableSourceModuleBuilder()
                .aggregate(action)
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> prepareSource(@NonNull Func2<Observable<T>, P, Observable<T>> func) {
        return new PreparableSourceModuleBuilder()
                .prepareSource(func)
                .add();
    }

    @NonNull
    public ApiBuilder2<T, P> add(@NonNull ProxyHandler<? extends Source<T, P>> module) {
        builder.add(module);
        return this;
    }

    @SafeVarargs
    @NonNull
    public final ApiBuilder2<T, P> addAll(@NonNull ProxyHandler<? extends Source<T, P>>... modules) {
        builder.addAll(modules);
        return this;
    }

    @NonNull
    public ApiBuilder2<T, P> addAll(@NonNull List<ProxyHandler<? extends Source<T, P>>> modules) {
        builder.addAll(modules);
        return this;
    }

    @NonNull
    public Source<T, P> source() {
        return builder.build();
    }

    @NonNull
    public RequestProcessor<T, P> requestProcessor() {
        return new RequestProcessor.Builder<T, P>()
                .setSource(source())
                .build();
    }

    @NonNull
    @Override
    public Api<T, P> build() {
        return new Api<T, P>(requestProcessor());
    }

    public final class SourceModuleBuilder {
        private final SourceProxyHandlerBuilder<T, P> builder = new SourceProxyHandlerBuilder<>();

        private SourceModuleBuilder() {}

        @NonNull
        public SourceModuleBuilder source(Action2<P, Subscriber<? super T>> source) {
            builder.setSource(source);
            return this;
        }

        @NonNull
        public SourceModuleBuilder source(Func1<P, T> source) {
            builder.setSource(source);
            return this;
        }

        @NonNull
        public ApiBuilder2<T, P> add() {
            return ApiBuilder2.this.add(builder.proxyHandler());
        }
    }

    public final class NetworkSourceModuleBuilder {
        private final NetworkSourceProxyHandlerBuilder<T, P> builder = new NetworkSourceProxyHandlerBuilder<>();

        private NetworkSourceModuleBuilder() {}

        @NonNull
        public NetworkSourceModuleBuilder requiresNetwork() {
            builder.setNetworkChecker();
            return this;
        }

        @NonNull
        public NetworkSourceModuleBuilder requiresNetwork(@NonNull NetworkChecker checker) {
            builder.setNetworkChecker(checker);
            return this;
        }

        @NonNull
        public ApiBuilder2<T, P> add() {
            return ApiBuilder2.this.add(builder.proxyHandler());
        }
    }

    public final class AggregableSourceModuleBuilder {
        private final AggregableSourceProxyHandlerBuilder<T, P> builder = new AggregableSourceProxyHandlerBuilder<>();

        private AggregableSourceModuleBuilder() {}

        @NonNull
        public AggregableSourceModuleBuilder aggregate(Func1<P, Aggregable> action) {
            builder.setAggregable(action);
            return this;
        }

        @NonNull
        public ApiBuilder2<T, P> add() {
            return ApiBuilder2.this.add(builder.proxyHandler());
        }
    }

    public final class PreparableSourceModuleBuilder {
        private final PreparableSourceProxyHandlerBuilder<T, P> builder = new PreparableSourceProxyHandlerBuilder<>();

        private PreparableSourceModuleBuilder() {}

        @NonNull
        public PreparableSourceModuleBuilder prepareSource(@NonNull Func2<Observable<T>, P, Observable<T>> func) {
            builder.setPrepareSourceRequest(func);
            return this;
        }

        @NonNull
        public ApiBuilder2<T, P> add() {
            return ApiBuilder2.this.add(builder.proxyHandler());
        }
    }
}
