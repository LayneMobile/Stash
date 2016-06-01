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

package stash.samples.hockeyloader.network.client;

import android.util.Log;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import stash.samples.hockeyloader.network.util.gson.GsonUtil;

public final class HockeyRestAdapter {
    private static final String ENDPOINT = "https://rink.hockeyapp.net/api/2";
    private static RestAdapter.Log DEFAULT_LOG;
    private static RestAdapter.LogLevel DEFAULT_LOGLEVEL;
    private static boolean LOGGING_ENABLED = true;
    private static volatile RestAdapter sInstance;

    private HockeyRestAdapter() {}

    static {
        initLogs();
    }

    public static boolean isLoggingEnabled() {
        return LOGGING_ENABLED;
    }

    public static void setLoggingEnabled(boolean loggingEnabled) {
        if (loggingEnabled != LOGGING_ENABLED) {
            LOGGING_ENABLED = loggingEnabled;
            initLogs();
            sInstance = null;
        }
    }

    private static void initLogs() {
        DEFAULT_LOG = LOGGING_ENABLED ? new DebugLog() : RestAdapter.Log.NONE;
        DEFAULT_LOGLEVEL = LOGGING_ENABLED ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;
    }

    public static RestAdapter getDefault() {
        if (sInstance == null) {
            synchronized (HockeyRestAdapter.class) {
                if (sInstance == null) {
                    sInstance = createDefault();
                }
            }
        }
        return sInstance;
    }

    private static RestAdapter createDefault() {
        return createDefault(ENDPOINT, DEFAULT_LOG, DEFAULT_LOGLEVEL);
    }

    public static RestAdapter createDefault(String endpoint, RestAdapter.Log log, RestAdapter.LogLevel logLevel) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setClient(new OkClient(HttpClient.getDefault()))
                .setConverter(new GsonConverter(GsonUtil.gson()))
                .setLog(log)
                .setLogLevel(logLevel)
                .build();
    }

    private static final class DebugLog implements RestAdapter.Log {
        @Override
        public void log(String s) {
            Log.i("Hockey Http", s);
        }
    }
}
