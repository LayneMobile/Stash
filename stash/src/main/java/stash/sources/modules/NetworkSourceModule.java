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
import android.support.annotation.Nullable;

import com.laynemobile.proxy.MethodResult;

import java.lang.reflect.Method;

import stash.SourceModuleBuilder;
import stash.sources.NetworkSource;
import stash.sources.builder.SourceHandler;
import stash.sources.builder.SourceMethodHandler;
import stash.util.NetworkChecker;

@stash.annotations.SourceModule(NetworkSource.class)
public final class NetworkSourceModule implements SourceModuleBuilder {
    private NetworkChecker networkChecker;

    @NonNull public NetworkSourceModule requiresNetwork() {
        this.networkChecker = null;
        return this;
    }

    @NonNull public NetworkSourceModule requiresNetwork(@Nullable NetworkChecker checker) {
        this.networkChecker = checker;
        return this;
    }

    @NonNull @Override public SourceHandler build() {
        return new SourceHandler.Builder(NetworkSource.class)
                .handle("getNetworkChecker", new Handler(networkChecker))
                .build();
    }

    private static final class Handler implements SourceMethodHandler {
        private final NetworkChecker networkChecker;

        private Handler(NetworkChecker networkChecker) {
            this.networkChecker = networkChecker;
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                result.set(networkChecker);
                return true;
            }
            return false;
        }
    }
}
