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

import stash.annotations.GenerateApiBuilder;
import stash.params.SimpleStashableParams;
import stash.sources.modules.AggregableSourceStashableModule;
import stash.sources.modules.NetworkSourceModule;
import stash.sources.modules.PreparableSourceSimpleModule;
import stash.sources.modules.SourceSimpleModule;
import stash.sources.modules.StashableSourceModule;

@GenerateApiBuilder({
        SourceSimpleModule.class,
        NetworkSourceModule.class,
        AggregableSourceStashableModule.class,
        StashableSourceModule.class,
        PreparableSourceSimpleModule.class
})
public class SimpleStashableApi<T> extends BaseApi<T, SimpleStashableParams> {
    protected SimpleStashableApi(@NonNull RequestProcessor<T, SimpleStashableParams> requestProcessor) {
        super(requestProcessor);
    }

    @NonNull public final Request<T> getRequest() {
        return requestProcessor().getRequest(SimpleStashableParams.DEFAULT);
    }

    @NonNull public final Request<T> getRequest(@NonNull StashPolicy stashPolicy) {
        return requestProcessor().getRequest(new SimpleStashableParams(stashPolicy));
    }

    @NonNull public final Request<Progress<T>> getProgressRequest() {
        return requestProcessor().getProgressRequest(SimpleStashableParams.DEFAULT);
    }

    @NonNull public final Request<Progress<T>> getProgressRequest(@NonNull StashPolicy stashPolicy) {
        return requestProcessor().getProgressRequest(new SimpleStashableParams(stashPolicy));
    }

    @NonNull public final Stash<T> getStash() {
        Stash<T> stash = null;
        Stashable<T> stashable = requestProcessor().getStashable(SimpleStashableParams.DEFAULT);
        if (stashable != null) {
            stash = stashable.getStash();
        }
        return stash == null ? Stash.<T>empty() : stash;
    }
}
