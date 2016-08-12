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

import com.laynemobile.proxy.annotations.GenerateProxyHandler;

import rx.Observable;
import stash.Params;
import stash.Request;
import stash.Source;
import stash.experimental.Processor;
import stash.experimental.RequestProcessor;

@GenerateProxyHandler
public interface PreparableSource<T, P extends Params> extends Source<T, P> {
    /**
     * Prepare the observable after it's created.
     *
     * @param sourceRequest
     *         the source request observable
     * @param p
     *         the parameters
     *
     * @return the prepared source request observable
     */
    @NonNull Observable<T> prepareSourceRequest(@NonNull Observable<T> sourceRequest, @NonNull P p);

    class Transformer<T, P extends Params> implements Processor.Interceptor.Transformer<PreparableSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        @Override public RequestProcessor.Interceptor<T, P> call(final PreparableSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    P p = chain.params();
                    Request<T> request = chain.proceed(p);
                    Observable<T> prepared = source.prepareSourceRequest(request.asObservable(), p);
                    return Request.from(prepared);
                }
            };
        }
    }
}
