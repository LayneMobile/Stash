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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.laynemobile.android.gson.GsonParcelable;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Value.Immutable
@Value.Enclosing
@Gson.TypeAdapters
public abstract class Auth extends GsonParcelable {
    private static final String LAST_USERNAME = "Auth.username";
    private static final AtomicReference<Auth> CURRENT_USER = new AtomicReference<>();

    @SerializedName("tokens")
    public abstract List<Token> getTokens();

    @SerializedName("key")
    public abstract String getKey();

    @SerializedName("name")
    public abstract String getName();

    @SerializedName("gravatar_hash")
    public abstract String getGravatarHash();

    @SerializedName("status")
    public abstract String getStatus();

    public static Auth getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(Auth currentUser) {
        CURRENT_USER.set(currentUser);
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

    @Value.Immutable
    public static abstract class Token extends GsonParcelable {
        @SerializedName("token")
        public abstract String getToken();

        @SerializedName("app_id")
        public abstract int getAppId();

        @Nullable
        @SerializedName("name")
        public abstract String getName();

        @SerializedName("rights")
        public abstract int getRights();
    }
}
