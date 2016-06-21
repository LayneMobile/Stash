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

import rx.functions.Func1;

public interface Processor<T, R> extends Func1<T, R> {

    interface Transformer<T1, T2, P extends Processor<T1, ?>>
            extends Func1<T2, P> {}

    interface Interceptor<T, R> {
        R intercept(Chain<T, R> chain);

        interface Chain<T, R> {
            T params();

            R proceed(T t);
        }

        interface Transformer<T, I extends Interceptor<?, ?>>
                extends Func1<T, I> {}
    }
}
