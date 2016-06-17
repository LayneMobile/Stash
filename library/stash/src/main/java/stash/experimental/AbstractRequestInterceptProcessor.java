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

package stash.experimental;

import org.immutables.value.Value;

import java.util.List;

import stash.Params;
import stash.Request;

@Value.Immutable
abstract class AbstractRequestInterceptProcessor<T, P extends Params> implements RequestProcessor<T, P> {

    abstract RequestProcessor<T, P> processor();

    abstract List<Interceptor<T, P>> interceptors();

    @Override public final Request<T> call(P p) {
        return new Child(0, p)
                .proceed(p);
    }

    private final class Child implements RequestProcessor.Interceptor.Chain<T, P> {
        private final int index;
        private final P params;

        private Child(int index, P params) {
            this.index = index;
            this.params = params;
        }

        @Override public P params() {
            return params;
        }

        @Override public Request<T> proceed(P p) {
            int index = this.index;
            List<Interceptor<T, P>> interceptors = interceptors();
            if (index < interceptors.size()) {
                Child chain = new Child(index + 1, p);
                return interceptors.get(index).intercept(chain);
            }
            return processor().call(p);
        }
    }
}
