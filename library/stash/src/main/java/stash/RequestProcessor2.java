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

import rx.Subscriber;

public interface RequestProcessor2<T, P extends Params> extends Processor {
    Request<T> request(P p);

    interface Source<T, P extends Params> extends Processor.Source {
        void call(P p, Subscriber<? super T> subscriber);
    }

    interface Builder<T, P extends Params> extends Processor.Builder<Source<T, P>, RequestProcessor2<T, P>> {

    }
}
