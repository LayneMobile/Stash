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


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import stash.Builder;
import stash.Params;
import stash.Source;

public final class SourceBuilder<T, P extends Params> implements Builder<Source<T, P>> {
    private final Set<SourceHandlerModule> modules = new HashSet<SourceHandlerModule>();

    public SourceBuilder<T, P> module(SourceHandlerModule module) {
        if (this.modules.contains(module)) {
            String msg = String.format("source type '%s' already defined", module.source);
            throw new IllegalStateException(msg);
        }
        this.modules.add(module);
        return this;
    }

    public SourceBuilder<T, P> modules(SourceHandlerModule... modules) {
        return modules(Arrays.asList(modules));
    }

    public SourceBuilder<T, P> modules(List<SourceHandlerModule> modules) {
        for (SourceHandlerModule module : modules) {
            module(module);
        }
        return this;
    }

    public boolean contains(Class<? extends Source> source) {
        for (SourceHandlerModule module : modules) {
            if (module.source.equals(source)) {
                return true;
            }
        }
        return false;
    }

    public void verifyContains(Class<? extends Source> source) {
        if (!contains(source)) {
            String msg = String.format("builder must have source type '%s'. You might have forgot a module", source);
            throw new IllegalStateException(msg);
        }
    }

    public void verifyContains(Collection<Class<? extends Source>> sources) {
        for (Class<? extends Source> source : sources) {
            verifyContains(source);
        }
    }

    @Override public Source<T, P> build() {
        if (modules.isEmpty()) {
            throw new IllegalStateException("no modules");
        } else if (!contains(Source.class)) {
            throw new IllegalStateException("must contain Source module");
        }
        return SourceHandler.source(modules);
    }
}
