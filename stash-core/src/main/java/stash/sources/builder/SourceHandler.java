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

package stash.sources.builder;

import com.laynemobile.proxy.MethodHandler;
import com.laynemobile.proxy.ProxyHandler;

import stash.Source;

public final class SourceHandler {
    final ProxyHandler<? extends Source> source;

    private SourceHandler(Builder builder) {
        this.source = builder.source.build();
    }

    public static final class Builder {
        private final ProxyHandler.Builder<? extends Source> source;

        public Builder(Class<? extends Source> source) {
            this.source = ProxyHandler.builder(source);
        }

        public MethodBuilder method(String methodName) {
            return new MethodBuilder(this, methodName);
        }

        public Builder handle(String methodName, MethodHandler handler) {
            source.handle(methodName, handler);
            return this;
        }

        public SourceHandler build() {
            return new SourceHandler(this);
        }
    }

    public static final class MethodBuilder {
        private final Builder builder;
        private final ProxyHandler.MethodBuilder<? extends Source> method;

        public MethodBuilder(Builder builder, String methodName) {
            this.builder = builder;
            this.method = builder.source.method(methodName);
        }

        public MethodBuilder handle(MethodHandler handler) {
            method.handle(handler);
            return this;
        }

        public Builder add() {
            method.add();
            return builder;
        }
    }
}
