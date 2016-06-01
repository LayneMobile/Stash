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


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Auth {

    private static final String LAST_USERNAME = "Auth.username";
    private static Auth sCurrentUser;

    @SerializedName("tokens")
    List<Token> tokens;

    @SerializedName("key")
    String key;

    @SerializedName("name")
    String name;

    @SerializedName("gravatar_hash")
    String gravatarHash;

    @SerializedName("status")
    String status;

    public static Auth getCurrentUser() {
        return sCurrentUser;
    }

    public static void setCurrentUser(Auth currentUser) {
        sCurrentUser = currentUser;
    }

    public static String getLastUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LAST_USERNAME, null);
    }

    public static void setLastUsername(Context context, String username) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(LAST_USERNAME, username)
                .apply();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public String getStatus() {
        return status;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getGravatarHash() {
        return gravatarHash;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "tokens=" + tokens +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", gravatarHash='" + gravatarHash + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Token implements Parcelable {
        public static final Creator<Token> CREATOR = new Creator<Token>() {
            @Override
            public Token createFromParcel(Parcel source) {
                return new Token(source);
            }

            @Override
            public Token[] newArray(int size) {
                return new Token[size];
            }
        };

        @SerializedName("token")
        private String token;

        @SerializedName("app_id")
        private int appId;

        @SerializedName("name")
        private String name;

        @SerializedName("rights")
        private int rights;

        public Token() {}

        private Token(Parcel in) {
            this.token = in.readString();
            this.appId = in.readInt();
            this.name = in.readString();
            this.rights = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(token);
            dest.writeInt(appId);
            dest.writeString(name);
            dest.writeInt(rights);
        }

        public String getToken() {
            return token;
        }

        public int getRights() {
            return rights;
        }

        public int getAppId() {
            return appId;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "token='" + token + '\'' +
                    ", appId=" + appId +
                    ", name='" + name + '\'' +
                    ", rights=" + rights +
                    '}';
        }

    }
}
