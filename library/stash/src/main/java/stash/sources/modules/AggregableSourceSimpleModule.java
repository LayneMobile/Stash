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

import rx.functions.Func1;
import stash.Aggregable;
import stash.SourceModuleBuilder;
import stash.aggregables.SimpleAggregable;
import stash.params.SimpleParams;
import stash.sources.AggregableSource;
import stash.sources.builder.SourceHandlerModule;

@stash.annotations.SourceModule(AggregableSource.class)
public final class AggregableSourceSimpleModule implements SourceModuleBuilder {
    private final AggregableSourceModule<SimpleParams> module = new AggregableSourceModule<SimpleParams>();

    @NonNull public AggregableSourceSimpleModule aggregate() {
        return aggregate(SimpleAggregable.DEFAULT_KEEP_ALIVE_SECONDS);
    }

    @NonNull public AggregableSourceSimpleModule aggregate(int keepAliveSeconds) {
        return aggregate(keepAliveSeconds, SimpleAggregable.DEFAULT_KEEP_ALIVE_ON_ERROR);
    }

    @NonNull public AggregableSourceSimpleModule aggregate(int keepAliveSeconds, boolean keepAliveOnError) {
        return aggregateInternal(this, keepAliveSeconds, keepAliveOnError);
    }

    @NonNull @Override public SourceHandlerModule build() {
        return module.build();
    }

    private static AggregableSourceSimpleModule aggregateInternal(@NonNull AggregableSourceSimpleModule module,
            final int keepAliveSeconds, final boolean keepAliveOnError) {
        // Create anonymous inner class in static context to avoid holding Module instance in memory
        module.module.aggregate(new Func1<SimpleParams, Aggregable>() {
            @Override public Aggregable call(SimpleParams simpleParams) {
                return new SimpleAggregable(SimpleParams.INSTANCE, keepAliveSeconds, keepAliveOnError);
            }
        });
        return module;
    }
}
