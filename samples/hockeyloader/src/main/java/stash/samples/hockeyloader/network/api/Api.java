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

package stash.samples.hockeyloader.network.api;


import stash.StashableApi;
import stash.samples.hockeyloader.network.model.AppVersions;
import stash.samples.hockeyloader.network.model.Apps;
import stash.samples.hockeyloader.network.model.Auth;

public final class Api {
    public static final StashableApi<Auth, AuthApi.Params> Auth = AuthApi.INSTANCE;
    public static final StashableApi<Apps, AppsApi.Params> Apps = AppsApi.INSTANCE;
    public static final StashableApi<AppVersions, AppVersionsApi.Params> AppVersions = AppVersionsApi.INSTANCE;

    private Api() { throw new AssertionError("no instances"); }
}
