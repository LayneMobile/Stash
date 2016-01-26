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

import com.shopify.buy.dataprovider.BuyClient;
import com.shopify.buy.dataprovider.BuyClientFactory;
import com.shopify.buy.model.Product;
import com.shopify.buy.model.ProductVariant;
import com.shopify.buy.model.Shop;
import com.shopify.buy.model.ShopifyHelper;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import stash.shopify.internal.ShopifyStashModule;
import stash.shopify.model.ProductList;
import stash.shopify.plugins.ShopifyConfig;
import stash.shopify.util.Util;

final class StoreClient {
    private static final StoreClient INSTANCE = new StoreClient();
    private static final int PAGE_SIZE = BuyClient.MAX_PAGE_SIZE;

    private final BuyClient shopifyClient;

    private StoreClient() {
        // Set up Shopify
        ShopifyConfig config = ShopifyStashModule.instance().getShopifyConfig();
        this.shopifyClient = BuyClientFactory.getBuyClient(config.domain(),
                config.apiKey(),
                config.channelId(),
                config.applicationName());
        this.shopifyClient.setPageSize(PAGE_SIZE);
    }

    static StoreClient getInstance() {
        return INSTANCE;
    }

    Observable<Shop> getShop() {
        return Observable.create(new Observable.OnSubscribe<Shop>() {
            @Override public void call(final Subscriber<? super Shop> subscriber) {
                if (subscriber.isUnsubscribed()) { return; }
                shopifyClient.getShop(new Callback<Shop>() {
                    @Override public void success(Shop shop, Response response) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(shop);
                            subscriber.onCompleted();
                        }
                    }

                    @Override public void failure(RetrofitError error) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(error);
                        }
                    }
                });
            }
        });
    }

    Observable<ProductList> getAllProducts() {
        return ShopifyApi.Shop.getRequest()
                .asObservable()
                .concatMap(new Func1<Shop, Observable<? extends ProductList>>() {
                    @Override public Observable<? extends ProductList> call(Shop shop) {
                        return getProductPage(0, shop, new ProductList());
                    }
                });
    }

    private Observable<ProductList> getProductPage(int page, final Shop shop,
            final ProductList list) {
        if (page < 0) { throw new IllegalArgumentException("no pages below 0"); }
        // Shopify uses 1 based index
        final int index = page + 1;
        return Observable.create(new Observable.OnSubscribe<List<Product>>() {
            @Override public void call(final Subscriber<? super List<Product>> subscriber) {
                if (subscriber.isUnsubscribed()) { return; }
                shopifyClient.getProductPage(index, new Callback<List<Product>>() {
                    @Override public void success(List<Product> products, Response response) {
                        products = Util.nullSafe(products);
                        for (Product product : products) {
                            // Fix for variant productId's being 0
                            for (ProductVariant variant : Util.nullSafe(product.getVariants())) {
                                ShopifyHelper.setVariantProductId(variant, product.getId());
                            }
                        }
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(products);
                            subscriber.onCompleted();
                        }
                    }

                    @Override public void failure(RetrofitError error) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(error);
                        }
                    }
                });
            }
        }).concatMap(new Func1<List<Product>, Observable<? extends ProductList>>() {
            @Override public Observable<? extends ProductList> call(List<Product> products) {
                list.addAll(products);
                boolean nextPage = products.size() >= PAGE_SIZE;
                nextPage |= !products.isEmpty() && list.size() < shop.getPublishedProductsCount();
                if (nextPage) {
                    return getProductPage(index + 1, shop, list);
                }
                return Observable.just(list);
            }
        });
    }
}
