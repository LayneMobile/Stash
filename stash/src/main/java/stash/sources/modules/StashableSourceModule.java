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

import com.laynemobile.proxy.MethodResult;

import java.lang.reflect.Method;

import rx.functions.Func1;
import stash.SourceModuleBuilder;
import stash.Stashable;
import stash.params.StashableParams;
import stash.sources.StashableSource;
import stash.sources.builder.SourceHandler;
import stash.sources.builder.SourceMethodHandler;

@stash.annotations.SourceModule(StashableSource.class)
public final class StashableSourceModule<T, P extends StashableParams<?>> implements SourceModuleBuilder {
    private Func1<P, Stashable<T>> func;

    @NonNull public StashableSourceModule<T, P> stash(@NonNull Func1<P, Stashable<T>> func) {
        this.func = func;
        return this;
    }

    @NonNull @Override public SourceHandler build() {
        if (func == null) {
            throw new IllegalStateException("source must not be null");
        }
        return new SourceHandler.Builder(StashableSource.class)
                .handle("getStashable", new Handler<T, P>(func))
                .build();
    }

    private static final class Handler<T, P extends StashableParams<?>> implements SourceMethodHandler {
        private final Func1<P, Stashable<T>> func;

        private Handler(Func1<P, Stashable<T>> func) {
            this.func = func;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(StashableParams.class)) {
                result.set(func.call((P) args[0]));
                return true;
            }
            return false;
        }
    }
}
