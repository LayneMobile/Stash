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

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import stash.StashModule;
import stash.shopify.plugins.ShopifyConfig;
import stash.shopify.plugins.ShopifyHook;
import stash.stashdbs.GsonDb;
import stash.util.AndroidLogger;

public class ShopifyApplication extends Application {

    private static ShopifyApplication sApplication;
    private static Context sContext;

    @Override public void onCreate() {
        super.onCreate();
        sApplication = this;
        sContext = getApplicationContext();

        StashModule.getInstance()
                .init(this)
                .setLogger(AndroidLogger.FULL)
                .registerShopifyHook(new ShopifyHook() {
                    @Override public ShopifyConfig getShopifyConfig() {
                        return Constants.SHOPIFY_CONFIG;
                    }

                    @Override public GsonDb.Config getGsonConfig() {
                        File cacheDir = new File(getCacheDir(), "stash");
                        return new GsonDb.Config(getGson(), cacheDir, 1, 10 * 1024 * 1024);
                    }

                    private Gson getGson() {
                        return new GsonBuilder()
                                .create();
                    }
                });
    }

    public static Context getContext() {
        return sContext;
    }

    public static ShopifyApplication getApplication() {
        return sApplication;
    }
}
