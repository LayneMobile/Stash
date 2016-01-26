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

package stash.samples.hockeyloader.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class AppVersions {

    @SerializedName("app_versions")
    List<AppVersion> appVersions;

    @SerializedName("status")
    String status;

    public List<AppVersion> getAppVersions() {
        if (appVersions == null) {
            return Collections.emptyList();
        }
        return appVersions;
    }

    public String getStatus() {
        return status;
    }
}
