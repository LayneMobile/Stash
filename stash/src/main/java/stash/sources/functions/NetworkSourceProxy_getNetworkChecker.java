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
import com.laynemobile.proxy.functions.Func0;

import stash.Params;
import stash.sources.functions.parent.AbstractNetworkSourceProxy_getNetworkChecker;
import stash.util.NetworkChecker;

@Generated
public class NetworkSourceProxy_getNetworkChecker<T, P extends Params> extends AbstractNetworkSourceProxy_getNetworkChecker<T, P> {
    public NetworkSourceProxy_getNetworkChecker(Func0<NetworkChecker> networkChecker) {
        super(networkChecker);
    }

    public NetworkSourceProxy_getNetworkChecker(final NetworkChecker networkChecker) {
        super(new Func0<NetworkChecker>() {
            @Override public NetworkChecker call() {
                return networkChecker;
            }
        });
    }

    public NetworkSourceProxy_getNetworkChecker() {
        super(new Func0<NetworkChecker>() {
            @Override public NetworkChecker call() {
                // TODO: plugin for default implementation
                return NetworkChecker.DEFAULT;
            }
        });
    }
}
