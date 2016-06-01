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

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.laynemobile.android.gson.GsonParcelable;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public abstract class App extends GsonParcelable {
    @SerializedName("title")
    public abstract String getTitle();

    @SerializedName("bundle_identifier")
    public abstract String getBundleIdentifier();

    @SerializedName("public_identifier")
    public abstract String getPublicIdentifier();

    @Nullable
    @SerializedName("device_family")
    public abstract String getDeviceFamily();

    @SerializedName("minimum_os_version")
    public abstract String getMinimumOsVersion();

    @SerializedName("release_type")
    public abstract int getReleaseType();

    @SerializedName("status")
    public abstract int getStatus();

    @SerializedName("platform")
    public abstract String getPlatform();
}
