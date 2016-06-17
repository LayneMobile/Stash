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

package stash.retrofit.internal;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import sourcerer.ExtensionMethod;
import sourcerer.ExtensionMethod.Kind;
import stash.annotations.Module;
import stash.retrofit.plugins.RetrofitHook;

@Module
public final class RetrofitStashModule {
    private static final RetrofitStashModule INSTANCE = new RetrofitStashModule();

    private final AtomicReference<RetrofitHook> hook = new AtomicReference<RetrofitHook>();

    private RetrofitStashModule() {}

    @ExtensionMethod(Kind.Instance) public static RetrofitStashModule instance() {
        return INSTANCE;
    }

    @ExtensionMethod(Kind.ReturnThis) public void registerRetrofitHook(@NonNull RetrofitHook hook) {
        if (!this.hook.compareAndSet(null, hook)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + this.hook.get());
        }
    }

    @ExtensionMethod(Kind.Return) public RetrofitHook getRetrofitHook() {
        if (hook.get() == null) {
            hook.compareAndSet(null, RetrofitHook.getDefaultInstance());
        }
        return hook.get();
    }
}
