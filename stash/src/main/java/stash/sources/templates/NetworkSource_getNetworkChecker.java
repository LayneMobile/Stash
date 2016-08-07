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

package stash.sources.templates;

import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;

import stash.Params;
import stash.sources.generated.AbstractNetworkSource_getNetworkChecker;
import stash.util.NetworkChecker;

@Generated
public class NetworkSource_getNetworkChecker<T, P extends Params> extends AbstractNetworkSource_getNetworkChecker<T, P> {
    public NetworkSource_getNetworkChecker(Func0<NetworkChecker> networkChecker) {
        super(networkChecker);
    }
}