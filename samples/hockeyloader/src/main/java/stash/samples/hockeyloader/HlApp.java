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

package stash.samples.hockeyloader;

import android.app.Application;

import retrofit.RestAdapter;
import stash.StashModule;
import stash.retrofit.plugins.RetrofitHook;
import stash.samples.hockeyloader.network.client.HockeyRestAdapter;
import stash.samples.hockeyloader.plugin.GsonHook;
import stash.util.AndroidLogger;


public class HlApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Configure stash module
        StashModule.getInstance()
                .init(this)
                .setLogger(AndroidLogger.FULL)
                .registerGsonDbHook(new GsonHook(this))
                .registerRetrofitHook(new RetrofitHook() {
                    @Override public RestAdapter defaultRestAdapter() {
                        return HockeyRestAdapter.getDefault();
                    }
                });
    }
}
