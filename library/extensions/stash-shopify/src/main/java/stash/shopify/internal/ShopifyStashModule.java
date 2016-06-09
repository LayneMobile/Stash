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

package stash.shopify.internal;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import sourcerer.InstanceMethod;
import sourcerer.ReturnThisMethod;
import stash.annotations.Module;
import stash.internal.GsonStashModuleImpl;
import stash.plugins.GsonDbHook;
import stash.shopify.plugins.ShopifyConfig;
import stash.shopify.plugins.ShopifyHook;
import stash.stashdbs.GsonDb;

@Module
public final class ShopifyStashModule {
    private static final ShopifyStashModule INSTANCE = new ShopifyStashModule();

    private final AtomicReference<ShopifyConfig> config = new AtomicReference<ShopifyConfig>();

    private ShopifyStashModule() {}

    @InstanceMethod public static ShopifyStashModule instance() {
        return INSTANCE;
    }

    @ReturnThisMethod public void registerShopifyHook(@NonNull ShopifyHook hook) {
        if (!config.compareAndSet(null, hook.getShopifyConfig())) {
            throw new IllegalStateException("hook already registered");
        }
        final GsonDb.Config gson = hook.getGsonConfig();
        if (gson != null) {
            GsonStashModuleImpl.getInstance().registerGsonDbHook(new GsonDbHook() {
                @Override public GsonDb.Config getGsonConfig() {
                    return gson;
                }
            });
        }
    }

    @NonNull public ShopifyConfig getShopifyConfig() {
        ShopifyConfig config = this.config.get();
        if (config == null) {
            throw new IllegalStateException("must register shopify config");
        }
        return config;
    }
}
