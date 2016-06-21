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
import stash.params.StashableParams;
import stash.sources.modules.AggregableSourceStashableModule;
import stash.sources.modules.NetworkSourceModule;
import stash.sources.modules.PreparableSourceModule;
import stash.sources.modules.SourceModule;
import stash.sources.modules.StashableSourceModule;

@GenerateApiBuilder({
        SourceModule.class,
        NetworkSourceModule.class,
        AggregableSourceStashableModule.class,
        StashableSourceModule.class,
        PreparableSourceModule.class
})
public class StashableApi<T, P extends StashableParams<?>> extends Api<T, P> {
    protected StashableApi(@NonNull RequestProcessor<T, P> requestProcessor) {
        super(requestProcessor);
    }

    @NonNull public final Stash<T> getStash(P p) {
        Stash<T> stash = null;
        Stashable<T> stashable = requestProcessor().getStashable(p);
        if (stashable != null) {
            stash = stashable.getStash();
        }
        return stash == null ? Stash.<T>empty() : stash;
    }
}
