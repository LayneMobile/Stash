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

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

public final class HttpClient {

    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    public static final int DEFAULT_READ_TIMEOUT_SECONDS = 60;

    private static OkHttpClient sInstance;

    private HttpClient() {}

    public static OkHttpClient getDefault() {
        if (sInstance == null) {
            synchronized (OkHttpClient.class) {
                if (sInstance == null) {
                    sInstance = createDefault();
                }
            }
        }
        return sInstance;
    }

    private static OkHttpClient createDefault() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        client.setReadTimeout(DEFAULT_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return client;
    }
}
