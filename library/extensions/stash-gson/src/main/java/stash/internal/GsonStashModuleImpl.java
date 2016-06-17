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

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import sourcerer.ExtensionMethod;
import sourcerer.ExtensionMethod.Kind;
import stash.annotations.Module;
import stash.plugins.GsonDbHook;

@Module
public final class GsonStashModuleImpl {
    private static final GsonStashModuleImpl INSTANCE = new GsonStashModuleImpl();

    private final AtomicReference<GsonDbHook> gsonDbHook = new AtomicReference<GsonDbHook>();

    private GsonStashModuleImpl() {}

    @ExtensionMethod(Kind.Instance) public static GsonStashModuleImpl getInstance() {
        return INSTANCE;
    }

    @ExtensionMethod(Kind.Return) @NonNull public GsonDbHook getGsonDbHook() {
        if (gsonDbHook.get() == null) {
            gsonDbHook.compareAndSet(null, GsonDbHook.getDefaultInstance());
            // we don't return from here but call get() again in case of thread-race so the winner will always get returned
        }
        return gsonDbHook.get();
    }

    @ExtensionMethod(Kind.ReturnThis) public void registerGsonDbHook(@NonNull GsonDbHook hook) {
        if (!gsonDbHook.compareAndSet(null, hook)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + gsonDbHook.get());
        }
    }
}
