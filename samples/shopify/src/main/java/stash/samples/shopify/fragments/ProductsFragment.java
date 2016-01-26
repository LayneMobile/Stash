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

package stash.samples.shopify.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.shopify.buy.model.Image;
import com.shopify.buy.model.Product;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.Bind;
import rxsubscriptions.Lifecycle;
import stash.StashPolicy;
import stash.exceptions.StashBaseException;
import stash.samples.shopify.R;
import stash.shopify.ShopifyApi;
import stash.shopify.model.ProductList;
import stash.shopify.util.Util;
import stash.subscribers.RefreshSubscriber;

public class ProductsFragment extends BaseFragment {
    private final ArrayList<Product> productList = new ArrayList<>();
    private Adapter adapter;
    private final RefreshListener refreshListener = new RefreshListener();

    // Views
    @Bind(R.id.swipey) SwipeRefreshLayout swipey;
    @Bind(R.id.list) ListView list;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new Adapter();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setAdapter(adapter);
        swipey.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                refresh();
            }
        });
    }

    @Override public void onDestroyView() {
        swipey.setOnRefreshListener(null);
        super.onDestroyView();
    }

    @Override public void onResume() {
        super.onResume();
        refresh(StashPolicy.DEFAULT);
    }

    private void refresh() {
        refresh(StashPolicy.SOURCE);
    }

    private void refresh(StashPolicy stashPolicy) {
        ShopifyApi.Products.getProgressRequest(stashPolicy)
                .onMain(subscriptions())
                .observeUntil(Lifecycle.OnStop)
                .subscribe(new Subscriber());
    }

    private class Subscriber extends RefreshSubscriber<ProductList> {
        public Subscriber() {
            super(refreshListener);
        }

        @Override protected void onNextData(ProductList products) {
            if (products != null) {
                productList.clear();
                productList.addAll(products);
                adapter.notifyDataSetChanged();
            }
        }

        @Override protected void onError(StashBaseException e) {
            Toast.makeText(getActivity(), "error getting products", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private class RefreshListener implements RefreshSubscriber.RefreshListener {
        private final AtomicInteger refreshCount = new AtomicInteger();

        @Override
        public void onRefreshStarted() {
            if (refreshCount.getAndIncrement() == 0 || !swipey.isRefreshing()) {
                swipey.setRefreshing(true);
            }
        }

        @Override
        public void onRefreshCompleted() {
            if (refreshCount.decrementAndGet() == 0 || swipey.isRefreshing()) {
                swipey.setRefreshing(false);
            }
        }
    }


    private class Adapter extends BaseAdapter {
        private Adapter() {}

        @Override
        public int getCount() {
            return productList.size();
        }

        @Override
        public Product getItem(int position) {
            return productList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            final Product product = getItem(position);
            final ViewHolder viewHolder = getViewHolder(convertView, viewGroup);

            viewHolder.title.setText(product.getTitle());
            viewHolder.description.setText(Html.fromHtml(product.getBodyHtml()));
            ImageView iv = viewHolder.image;
            Image image = Util.getFirst(product.getImages());
            if (image == null) {
                iv.setImageResource(R.drawable.placeholder);
            } else {
                Glide.with(ProductsFragment.this)
                        .load(image.getSrc())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(iv);
            }
            return viewHolder.convertView;
        }

        private ViewHolder getViewHolder(View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(R.layout.fragment_product_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.convertView = convertView;
                viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.description = (TextView) convertView.findViewById(R.id.description);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            return viewHolder;
        }
    }

    private static class ViewHolder {
        private View convertView;
        private ImageView image;
        private TextView title;
        private TextView description;
    }
}
