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

package stash.samples.shopify;

import android.support.annotation.NonNull;

import stash.shopify.plugins.ShopifyConfig;

public final class Constants {
    public static final String SHOPIFY_DOMAIN = "";
    public static final String SHOPIFY_API_KEY = "";
    public static final String SHOPIFY_CHANNEL_ID = "";
    public static final ShopifyConfig SHOPIFY_CONFIG = new ShopifyConfig() {
        @NonNull @Override public String domain() {
            return SHOPIFY_DOMAIN;
        }

        @NonNull @Override public String apiKey() {
            return SHOPIFY_API_KEY;
        }

        @NonNull @Override public String channelId() {
            return SHOPIFY_CHANNEL_ID;
        }

        @NonNull @Override public String applicationName() {
            return ShopifyApplication.getApplication().getPackageName();
        }
    };

    private Constants() { throw new AssertionError("no instances"); }
}
