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

package stash.shopify;


import com.shopify.buy.model.Shop;

import java.util.concurrent.TimeUnit;

import rx.functions.Func1;
import stash.SimpleStashableApi;
import stash.SimpleStashableApiBuilder;
import stash.Stashable;
import stash.internal.GsonStashesImpl;
import stash.internal.StashesImpl;
import stash.params.SimpleStashableParams;

final class ShopApi {
    private ShopApi() { throw new AssertionError("no instances"); }

    static {
        StashesImpl.getInstance().memDb().registerMaxSize(Shop.class, 1);
    }

    static final SimpleStashableApi<Shop> INSTANCE = new SimpleStashableApiBuilder<Shop>()
            .source(StoreClient.getInstance().getShop())
            .requiresNetwork()
            .stash(new Func1<SimpleStashableParams, Stashable<Shop>>() {
                @Override public Stashable<Shop> call(SimpleStashableParams params) {
                    return new Stashable.Builder<Shop, String>(Shop.class, params)
                            .primary(StashesImpl.getInstance().memDb())
                            .secondary(GsonStashesImpl.getInstance().gsonDb())
                            .maxAge(TimeUnit.HOURS, 1)
                            .build();
                }
            })
            .aggregate()
            .build();
}
