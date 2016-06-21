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

import java.util.concurrent.TimeUnit;

import rx.functions.Func1;
import stash.SimpleStashableApi;
import stash.SimpleStashableApiBuilder;
import stash.Stashable;
import stash.internal.GsonStashesImpl;
import stash.internal.StashesImpl;
import stash.params.SimpleStashableParams;
import stash.shopify.model.ProductList;

final class ProductsApi {
    private ProductsApi() { throw new AssertionError("no instances"); }

    static {
        StashesImpl.getInstance().memDb().registerMaxSize(ProductList.class, 1);
    }

    static final SimpleStashableApi<ProductList> INSTANCE = new SimpleStashableApiBuilder<ProductList>()
            .source(StoreClient.getInstance().getAllProducts())
            .requiresNetwork()
            .stash(new Func1<SimpleStashableParams, Stashable<ProductList>>() {
                @Override public Stashable<ProductList> call(SimpleStashableParams params) {
                    return new Stashable.Builder<ProductList, String>(ProductList.class, params)
                            .primary(StashesImpl.getInstance().memDb())
                            .secondary(GsonStashesImpl.getInstance().gsonDb())
                            .maxAge(TimeUnit.HOURS, 2)
                            .build();
                }
            })
            .aggregate()
            .build();
}
