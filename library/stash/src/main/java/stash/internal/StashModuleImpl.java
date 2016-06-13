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

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import sourcerer.ExtensionMethod;
import sourcerer.ExtensionMethod.Kind;
import stash.annotations.Module;
import stash.plugins.MemDbHook;
import stash.plugins.StashSchedulersHook;
import stash.util.Logger;

@Module
public class StashModuleImpl {
    private static final StashModuleImpl INSTANCE = new StashModuleImpl();

    private final AtomicReference<StashSchedulersHook> schedulersHook = new AtomicReference<StashSchedulersHook>();
    private final AtomicReference<MemDbHook> memSourceHook = new AtomicReference<MemDbHook>();
    private volatile Context context;

    private StashModuleImpl() {}

    @ExtensionMethod(Kind.Instance) @NonNull public static StashModuleImpl getInstance() {
        return INSTANCE;
    }

    @ExtensionMethod(Kind.ReturnThis) @NonNull public StashModuleImpl init(@NonNull Context context) {
        if (this.context == null) {
            if (context instanceof Application) {
                this.context = context;
            } else if (context != null) {
                this.context = context.getApplicationContext();
            }
        }
        return this;
    }

    @ExtensionMethod(Kind.Return) @Nullable public Context getContext() {
        return context;
    }

    @ExtensionMethod(Kind.ReturnThis) @NonNull
    public StashModuleImpl setLogger(@NonNull Logger logger) {
        StashLog.setLogger(logger);
        return this;
    }

    @ExtensionMethod(Kind.Return) @NonNull public StashSchedulersHook getSchedulersHook() {
        StashSchedulersHook hook = schedulersHook.get();
        if (hook == null) {
            schedulersHook.compareAndSet(null, StashSchedulersHook.getDefaultInstance());
            return schedulersHook.get();
        }
        return hook;
    }

    @ExtensionMethod(Kind.ReturnThis) @NonNull
    public StashModuleImpl registerSchedulersHook(@NonNull StashSchedulersHook hook) {
        if (!schedulersHook.compareAndSet(null, hook)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + schedulersHook.get());
        }
        return this;
    }

    @ExtensionMethod(Kind.Return) @NonNull public MemDbHook getMemDbHook() {
        MemDbHook hook = memSourceHook.get();
        if (hook == null) {
            memSourceHook.compareAndSet(null, MemDbHook.getDefaultInstance());
            return memSourceHook.get();
        }
        return hook;
    }

    @ExtensionMethod(Kind.ReturnThis) @NonNull public StashModuleImpl registerMemDbHook(@NonNull MemDbHook hook) {
        if (!memSourceHook.compareAndSet(null, hook)) {
            throw new IllegalArgumentException(
                    "Another strategy was already registered: " + memSourceHook.get());
        }
        return this;
    }
}
