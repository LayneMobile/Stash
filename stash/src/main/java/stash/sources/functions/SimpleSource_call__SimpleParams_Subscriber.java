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
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Func0;

import rx.Observable;
import rx.Subscriber;
import stash.functions.Source_call__P_Subscriber;
import stash.params.SimpleParams;

@Generated
public class SimpleSource_call__SimpleParams_Subscriber<T> extends Source_call__P_Subscriber<T, SimpleParams> {
    public SimpleSource_call__SimpleParams_Subscriber(Action1<Subscriber<? super T>> source) {
        super(source);
    }

    public SimpleSource_call__SimpleParams_Subscriber(Func0<T> source) {
        super(source);
    }

    public SimpleSource_call__SimpleParams_Subscriber(Observable<T> source) {
        super(source);
    }
}
