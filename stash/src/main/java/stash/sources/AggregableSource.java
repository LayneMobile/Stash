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

package stash.sources;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.laynemobile.proxy.annotations.GenerateProxyHandler;
import com.laynemobile.proxy.annotations.GenerateProxyHandlerFunction;

import stash.Aggregable;
import stash.Params;
import stash.Request;
import stash.Source;
import stash.experimental.Processor;
import stash.experimental.RequestProcessor;

@GenerateProxyHandler
public interface AggregableSource<T, P extends Params> extends Source<T, P> {
    @GenerateProxyHandlerFunction("aggregable")
    @Nullable Aggregable getAggregable(@NonNull P p);

    class Transformer<T, P extends Params> implements Processor.Interceptor.Transformer<AggregableSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        @Override public RequestProcessor.Interceptor<T, P> call(final AggregableSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    P p = chain.params();
                    Aggregable aggregable = source.getAggregable(p);
                    if (aggregable != null) {
                        Object key = aggregable.key();
                        // TODO: look for existing
                        /*
                            Aggregate aggregate = find(key);
                            if (aggregate == null) {
                                Request<T> source = chain.proceed(p);
                                aggregate = new Aggregate(source);
                                put(key, aggregate);
                            }
                            return aggregate.request;
                         */
                    }
                    return chain.proceed(p);
                }
            };
        }
    }
}
