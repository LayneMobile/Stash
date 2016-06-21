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

import android.content.Context;
import android.support.annotation.Nullable;

import stash.Api;
import stash.Params;
import stash.Request;
import stash.Source;
import stash.exceptions.NetworkUnavailableException;
import stash.experimental.Processor;
import stash.experimental.RequestProcessor;
import stash.internal.StashLog;
import stash.util.NetworkChecker;

public interface NetworkSource<T, P extends Params> extends Source<T, P> {
    @Nullable NetworkChecker getNetworkChecker();

    class Transformer<T, P extends Params> implements Processor.Interceptor.Transformer<NetworkSource<T, P>, RequestProcessor.Interceptor<T, P>> {
        private static final String TAG = Transformer.class.getSimpleName();

        @Override public RequestProcessor.Interceptor<T, P> call(final NetworkSource<T, P> source) {
            return new RequestProcessor.Interceptor<T, P>() {
                @Override public Request<T> intercept(Processor.Interceptor.Chain<P, Request<T>> chain) {
                    P p = chain.params();
                    StashLog.d(TAG, "checking network connection");
                    Context context = Api.getContext(p);
                    if (context == null) {
                        StashLog.w(TAG, "context is null. unable to check network connection");
                    } else {
                        NetworkChecker networkChecker = source.getNetworkChecker();
                        if (networkChecker == null) {
                            networkChecker = NetworkChecker.DEFAULT;
                        }
                        if (!networkChecker.isNetworkAvailable(context)) {
                            StashLog.i(TAG, "not running request. no network");
                            return Request.error(new NetworkUnavailableException("no network"));
                        }
                    }
                    return chain.proceed(p);
                }
            };
        }
    }
}
