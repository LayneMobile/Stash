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

public class App implements Parcelable {

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel source) {
            return new App(source);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };

    @SerializedName("title")
    String title;

    @SerializedName("bundle_identifier")
    String bundleIdentifier;

    @SerializedName("public_identifier")
    String publicIdentifier;

    @SerializedName("device_family")
    String deviceFamily;

    @SerializedName("minimum_os_version")
    String minimumOsVersion;

    @SerializedName("release_type")
    int releaseType;

    @SerializedName("status")
    int status;

    @SerializedName("platform")
    String platform;

    public App() {}

    private App(Parcel in) {
        this.title = in.readString();
        this.bundleIdentifier = in.readString();
        this.publicIdentifier = in.readString();
        this.deviceFamily = in.readString();
        this.minimumOsVersion = in.readString();
        this.releaseType = in.readInt();
        this.status = in.readInt();
        this.platform = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(bundleIdentifier);
        dest.writeString(publicIdentifier);
        dest.writeString(deviceFamily);
        dest.writeString(minimumOsVersion);
        dest.writeInt(releaseType);
        dest.writeInt(status);
        dest.writeString(platform);
    }

    public String getPlatform() {
        return platform;
    }

    public String getTitle() {
        return title;
    }

    public String getBundleIdentifier() {
        return bundleIdentifier;
    }

    public String getPublicIdentifier() {
        return publicIdentifier;
    }

    public String getDeviceFamily() {
        return deviceFamily;
    }

    public String getMinimumOsVersion() {
        return minimumOsVersion;
    }

    public int getReleaseType() {
        return releaseType;
    }

    public int getStatus() {
        return status;
    }
}
