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
import com.laynemobile.proxy.annotations.GenerateProxyFunction;

import rx.Subscriber;
import stash.Source;
import stash.params.SimpleParams;

@GenerateProxyBuilder(extendsFrom = Source.class)
public interface SimpleSource<T> extends Source<T, SimpleParams> {
    @GenerateProxyFunction("source")
    @Override void call(SimpleParams ignored, Subscriber<? super T> subscriber);
}
