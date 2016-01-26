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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stash.Source;

public final class SourceHandlerModule {
    final Class<? extends Source> source;
    final Map<String, List<SourceMethodHandler>> handlers;

    private SourceHandlerModule(Builder builder) {
        this.source = builder.source;
        this.handlers = Collections.unmodifiableMap(builder.handlers);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceHandlerModule module = (SourceHandlerModule) o;

        return source.equals(module.source);
    }

    @Override public int hashCode() {
        return source.hashCode();
    }

    public static final class Builder {
        private final Class<? extends Source> source;
        private final Map<String, List<SourceMethodHandler>> handlers
                = new HashMap<String, List<SourceMethodHandler>>();

        public Builder(Class<? extends Source> source) {
            this.source = source;
        }

        public MethodBuilder method(String methodName) {
            return new MethodBuilder(this, methodName);
        }

        public Builder handle(String methodName, SourceMethodHandler handler) {
            return method(methodName)
                    .handle(handler)
                    .add();
        }

        public SourceHandlerModule build() {
            return new SourceHandlerModule(this);
        }

        private Builder add(MethodBuilder builder) {
            List<SourceMethodHandler> handlerList = this.handlers.get(builder.methodName);
            if (handlerList == null) {
                handlerList = new ArrayList<SourceMethodHandler>();
                this.handlers.put(builder.methodName, handlerList);
            }
            handlerList.add(builder.handler);
            return this;
        }
    }

    public static final class MethodBuilder {
        private final Builder builder;
        private final String methodName;
        private SourceMethodHandler handler;

        private MethodBuilder(Builder builder, String methodName) {
            this.builder = builder;
            this.methodName = methodName;
        }

        public MethodBuilder handle(SourceMethodHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder add() {
            if (handler == null) {
                throw new IllegalArgumentException("must set a handler");
            }
            return builder.add(this);
        }
    }
}
