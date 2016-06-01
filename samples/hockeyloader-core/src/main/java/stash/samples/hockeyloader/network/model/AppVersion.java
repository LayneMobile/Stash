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

import java.util.Date;
import java.util.List;

@Value.Immutable
@Gson.TypeAdapters
public abstract class AppVersion extends GsonParcelable {
    @SerializedName("version")
    public abstract String getVersion();

    @SerializedName("shortversion")
    public abstract String getShortVersion();

    @SerializedName("title")
    public abstract String getTitle();

    @SerializedName("timestamp")
    public abstract long getTimestamp();

    @SerializedName("appsize")
    public abstract long getAppsize();

    @SerializedName("notes")
    public abstract String getNotes();

    @SerializedName("mandatory")
    public abstract boolean isMandatory();

    @SerializedName("external")
    public abstract boolean isExternal();

    @Nullable
    @SerializedName("device_family")
    public abstract String getDeviceFamily();

    @SerializedName("id")
    public abstract int getId();

    @SerializedName("app_id")
    public abstract int getAppId();

    @Nullable
    @SerializedName("minimum_os_version")
    public abstract String getMinimumOsVersion();

    @Nullable
    @SerializedName("download_url")
    public abstract String getDownloadUrl();

    @SerializedName("config_url")
    public abstract String getConfigUrl();

    @SerializedName("restricted_to_tags")
    public abstract boolean isRestrictedToTags();

    @SerializedName("status")
    public abstract int getStatus();

    @SerializedName("tags")
    public abstract List<String> getTags();

    @SerializedName("created_at")
    public abstract Date getCreatedAt();

    @SerializedName("updated_at")
    public abstract Date getUpdatedAt();
}
