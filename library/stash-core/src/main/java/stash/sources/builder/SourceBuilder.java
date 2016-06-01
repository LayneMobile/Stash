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


import java.util.Collection;
import java.util.List;

import stash.Builder;
import stash.Params;
import stash.Source;
import stash.types.TypeBuilder;
import stash.types.TypeHandler;

public final class SourceBuilder<T, P extends Params> implements Builder<Source<T, P>> {
    private final TypeBuilder<Source> source;

    public SourceBuilder() {
        this.source = new TypeBuilder<>(Source.class);
    }

    public boolean contains(Class<? extends Source> type) {
        return source.contains(type);
    }

    public TypeBuilder<Source> module(SourceHandler module) {
        return source.module(module);
    }

    public void verifyContains(Class<? extends Source> type) {
        source.verifyContains(type);
    }

    public TypeBuilder<Source> modules(List<SourceHandler> modules) {
        return source.modules(modules);
    }

    public void verifyContains(Collection<Class<? extends Source>> types) {
        source.verifyContains(types);
    }

    @SafeVarargs public final TypeBuilder<Source> modules(TypeHandler<? extends Source>... modules) {
        return source.modules(modules);
    }

    @SuppressWarnings("unchecked")
    @Override public Source<T, P> build() {
        return (Source<T, P>) source.build();
    }
}
