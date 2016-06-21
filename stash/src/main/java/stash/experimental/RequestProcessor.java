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

import stash.Params;
import stash.Request;

public interface RequestProcessor<T, P extends Params> extends Processor<P, Request<T>> {

    interface Interceptor<T, P extends Params>
            extends Processor.Interceptor<P, Request<T>> {

        interface Chain<T, P extends Params>
                extends Processor.Interceptor.Chain<P, Request<T>> {}
    }
}
