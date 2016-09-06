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

package stash.sources.functions;

import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Func2;

import rx.Observable;
import stash.Params;
import stash.sources.functions.parent.AbstractPreparableSourceProxy_prepareSourceRequest__Observable_P;

@Generated
public class PreparableSourceProxy_prepareSourceRequest__Observable_P<T, P extends Params> extends AbstractPreparableSourceProxy_prepareSourceRequest__Observable_P<T, P> {
    public PreparableSourceProxy_prepareSourceRequest__Observable_P(
            Func2<Observable<T>, P, Observable<T>> prepareSourceRequest) {
        super(prepareSourceRequest);
    }

    public PreparableSourceProxy_prepareSourceRequest__Observable_P(
            final Func1<Observable<T>, Observable<T>> prepareSourceRequest) {
        super(new Func2<Observable<T>, P, Observable<T>>() {
            @Override public Observable<T> call(Observable<T> observable, P p) {
                return prepareSourceRequest.call(observable);
            }
        });
    }
}
