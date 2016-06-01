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

package stash.samples.hockeyloader.plugin;

import android.content.Context;

import com.google.gson.Gson;

import java.io.File;

import stash.plugins.GsonDbHook;
import stash.samples.hockeyloader.BuildConfig;
import stash.stashdbs.GsonDb;

public class GsonHook extends GsonDbHook {
    private static final String CACHE_DIR = "network";
    private static final int VERSION = BuildConfig.VERSION_CODE;
    private static final int SIZE = 10 * 1024 * 1024; // 10 MB

    private final Context mContext;
    private final Gson mGson;

    public GsonHook(Context context, Gson gson) {
        this.mContext = context.getApplicationContext();
        this.mGson = gson;
    }

    private static File cacheDir(Context context) {
        return new File(context.getCacheDir(), CACHE_DIR);
    }

    @Override
    public GsonDb.Config getGsonConfig() {
        return new GsonDb.Config(mGson, cacheDir(mContext), VERSION, SIZE);
    }
}
