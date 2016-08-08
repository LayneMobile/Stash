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

import com.laynemobile.proxy.annotations.GenerateProxyBuilder;

import stash.Request;
import stash.Source;
import stash.StashPolicy;
import stash.StashableProcessor;
import stash.experimental.Processor;
import stash.experimental.RequestProcessor;
import stash.params.StashableParams;

@GenerateProxyBuilder
public interface StashableSource<T, P extends StashableParams<?>>
        extends Source<T, P>,
        StashableProcessor<T, P> {

    final class Transformer<T, P extends StashableParams<?>>
            implements Processor.Interceptor.Transformer<StashableSource<T, P>, RequestProcessor.Interceptor<T, P>> {

        private static final Transformer<Object, StashableParams<?>> INSTANCE = new Transformer<>();

        private Transformer() {}

        @SuppressWarnings("unchecked")
        public static <T, P extends StashableParams<?>> Transformer<T, P> instance() {
            return (Transformer<T, P>) INSTANCE;
        }

        @Override public RequestProcessor.Interceptor<T, P> call(final StashableSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    final P p = chain.params();
                    final StashPolicy stashPolicy = p.getStashPolicy();
                    if (stashPolicy == StashPolicy.SOURCE_ONLY_NO_STASH) {
                        return chain.proceed(p);
                    }

                    // TODO: see what the stash looks like, etc
//                    final Stashable<T> stashable = source.getStashable(p);
                    return chain.proceed(p);
                }
            };
        }
    }
}
