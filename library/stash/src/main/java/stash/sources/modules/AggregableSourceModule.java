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

import rx.functions.Func1;
import stash.Aggregable;
import stash.Params;
import stash.SourceModuleBuilder;
import stash.sources.AggregableSource;
import stash.sources.builder.SourceHandler;
import stash.sources.builder.SourceMethodHandler;
import stash.types.MethodResult;

@stash.annotations.SourceModule(AggregableSource.class)
public final class AggregableSourceModule<P extends Params> implements SourceModuleBuilder {
    private Func1<P, Aggregable> action;

    public AggregableSourceModule<P> aggregate(@NonNull Func1<P, Aggregable> action) {
        this.action = action;
        return this;
    }

    @NonNull @Override public SourceHandler build() {
        if (action == null) {
            throw new IllegalStateException("source must be set");
        }
        return new SourceHandler.Builder(AggregableSource.class)
                .handle("getAggregable", new Handler<P>(action))
                .build();
    }

    private static final class Handler<P extends Params> implements SourceMethodHandler {
        private final Func1<P, Aggregable> action;

        private Handler(Func1<P, Aggregable> action) {
            this.action = action;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(Params.class)) {
                result.set(action.call((P) args[0]));
                return true;
            }
            return false;
        }
    }
}
