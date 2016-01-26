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

package stash.internal;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import stash.Stash;
import stash.StashCollection;
import stash.StashDb;
import stash.StashKey;
import stash.annotations.InstanceMethod;
import stash.annotations.ReturnMethod;
import stash.annotations.Stashes;
import stash.stashdbs.FileDb;
import stash.stashdbs.GsonDb;

@Stashes
public final class GsonStashesImpl {
    private static final GsonStashesImpl INSTANCE = new GsonStashesImpl();

    private final AtomicReference<GsonDb.Config> gsonConfig = new AtomicReference<GsonDb.Config>();
    private volatile FileDb gsonDb;

    private GsonStashesImpl() {}

    @InstanceMethod public static GsonStashesImpl getInstance() {
        return INSTANCE;
    }

    @ReturnMethod @NonNull public StashDb<String> gsonDb() {
        FileDb gsonDb = this.gsonDb;
        if (gsonDb == null || gsonDb.isClosed()) {
            GsonDb.Config gsonConfig = getGsonConfig();
            synchronized (this) {
                gsonDb = this.gsonDb;
                if (gsonDb == null || gsonDb.isClosed()) {
                    try {
                        this.gsonDb = gsonDb = GsonDb.open(gsonConfig);
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                "Unable to create GsonDb with config: " + gsonConfig, e);
                    }
                }
            }
        }
        return StashDb.create(gsonDb);
    }

    @ReturnMethod @NonNull public <T> StashCollection<String, T> gsonCollection(@NonNull Class<T> type) {
        return gsonDb().getStashCollection(type);
    }

    @ReturnMethod @NonNull public <T> Stash<T> gson(@NonNull Class<T> type, @NonNull StashKey<String> stashKey) {
        return gsonDb().getStash(type, stashKey);
    }

    @NonNull private GsonDb.Config getGsonConfig() {
        GsonDb.Config config = gsonConfig.get();
        if (config == null) {
            GsonDb.Config gson = GsonStashModuleImpl.getInstance().getGsonDbHook().getGsonConfig();
            if (gson == null) {
                Context context = StashModuleImpl.getInstance().getContext();
                if (context == null) {
                    throw new IllegalStateException("Must call init() on stash.Module before accessing gson stash");
                }
                gson = GsonDb.Config.getDefault(context);
            }
            gsonConfig.compareAndSet(null, gson);
            return gsonConfig.get();
        }
        return config;
    }
}
