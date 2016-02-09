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

import stash.BaseApi;
import stash.Progress;
import stash.Request;
import stash.RequestProcessor;
import stash.annotations.GenerateApiBuilder;
import stash.params.SimpleParams;
import stash.sources.modules.AggregableSourceSimpleModule;
import stash.sources.modules.NetworkSourceModule;
import stash.sources.modules.PreparableSourceSimpleModule;
import stash.sources.modules.SourceSimpleModule;

@GenerateApiBuilder({
        SourceSimpleModule.class,
        NetworkSourceModule.class,
        AggregableSourceSimpleModule.class,
        PreparableSourceSimpleModule.class
})
public class SimpleApi<T> extends BaseApi<T, SimpleParams> {
    protected SimpleApi(@NonNull RequestProcessor<T, SimpleParams> requestProcessor) {
        super(requestProcessor);
    }

    public final Request<T> getRequest() {
        return requestProcessor()
                .getRequest(SimpleParams.INSTANCE);
    }

    public final Request<Progress<T>> getProgressRequest() {
        return requestProcessor()
                .getProgressRequest(SimpleParams.INSTANCE);
    }
}