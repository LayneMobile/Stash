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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class AppVersion implements Parcelable {

    public static Creator<AppVersion> CREATOR = new Creator<AppVersion>() {
        @Override
        public AppVersion createFromParcel(Parcel source) {
            return new AppVersion(source);
        }

        @Override
        public AppVersion[] newArray(int size) {
            return new AppVersion[size];
        }
    };

    @SerializedName("version")
    private String version;

    @SerializedName("shortversion")
    private String shortVersion;

    @SerializedName("title")
    private String title;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("appsize")
    private long appsize;

    @SerializedName("notes")
    private String notes;

    @SerializedName("mandatory")
    private boolean mandatory;

    @SerializedName("external")
    private boolean external;

    @SerializedName("device_family")
    private String deviceFamily;

    @SerializedName("id")
    private int id;

    @SerializedName("app_id")
    private int appId;

    @SerializedName("minimum_os_version")
    private String minimumOsVersion;

    @SerializedName("download_url")
    private String downloadUrl;

    @SerializedName("config_url")
    private String configUrl;

    @SerializedName("restricted_to_tags")
    private boolean restrictedToTags;

    @SerializedName("status")
    private int status;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    public AppVersion() {}

    private AppVersion(Parcel in) {
        this.version = in.readString();
        this.shortVersion = in.readString();
        this.title = in.readString();
        this.timestamp = in.readLong();
        this.appsize = in.readLong();
        this.notes = in.readString();
        this.mandatory = in.readInt() == 1;
        this.external = in.readInt() == 1;
        this.deviceFamily = in.readString();
        this.id = in.readInt();
        this.appId = in.readInt();
        this.minimumOsVersion = in.readString();
        this.downloadUrl = in.readString();
        this.configUrl = in.readString();
        this.restrictedToTags = in.readInt() == 1;
        this.status = in.readInt();
        long date = in.readLong();
        this.createdAt = date == 0 ? null : new Date(date);
        date = in.readLong();
        this.updatedAt = date == 0 ? null : new Date(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(version);
        dest.writeString(shortVersion);
        dest.writeString(title);
        dest.writeLong(timestamp);
        dest.writeLong(appsize);
        dest.writeString(notes);
        dest.writeInt(mandatory ? 1 : 0);
        dest.writeInt(external ? 1 : 0);
        dest.writeString(deviceFamily);
        dest.writeInt(id);
        dest.writeInt(appId);
        dest.writeString(minimumOsVersion);
        dest.writeString(downloadUrl);
        dest.writeString(configUrl);
        dest.writeInt(restrictedToTags ? 1 : 0);
        dest.writeInt(status);
        dest.writeStringList(tags);
        dest.writeLong(createdAt == null ? 0L : createdAt.getTime());
        dest.writeLong(updatedAt == null ? 0L : updatedAt.getTime());
    }

    public String getVersion() {
        return version;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAppsize() {
        return appsize;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isExternal() {
        return external;
    }

    public String getDeviceFamily() {
        return deviceFamily;
    }

    public int getId() {
        return id;
    }

    public int getAppId() {
        return appId;
    }

    public String getMinimumOsVersion() {
        return minimumOsVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public boolean isRestrictedToTags() {
        return restrictedToTags;
    }

    public int getStatus() {
        return status;
    }

    public List<String> getTags() {
        return tags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
