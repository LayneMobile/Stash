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

package stash.retrofit;

import android.support.annotation.NonNull;

import stash.Api;
import stash.Params;
import stash.RequestProcessor;
import stash.annotations.GenerateApiBuilder;
import stash.sources.modules.AggregableSourceModule;
import stash.sources.modules.PreparableSourceModule;

@GenerateApiBuilder({
        RetrofitSourceModule.class,
        AggregableSourceModule.class,
        PreparableSourceModule.class
})
public class RetrofitApi<T, P extends Params, S> extends Api<T, P> {
    protected RetrofitApi(@NonNull RequestProcessor<T, P> requestProcessor) {
        super(requestProcessor);
    }
}
