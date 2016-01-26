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

package stash.samples.hockeyloader.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.Bind;
import rx.Subscriber;
import stash.StashPolicy;
import stash.samples.hockeyloader.R;

public abstract class ListFragment extends HockeyFragment {
    // Views
    @Bind(R.id.swipey) SwipeRefreshLayout swipey;
    @Bind(R.id.list) ListView list;

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    protected void refresh() {
        refresh(StashPolicy.SOURCE);
    }

    protected abstract void refresh(StashPolicy stashPolicy);

    abstract class RefreshSubscriber<T> extends stash.subscribers.RefreshSubscriber<T> {
        RefreshSubscriber() {
            super(new ListFragment.RefreshListener());
        }

        RefreshSubscriber(Subscriber<?> op) {
            super(op, new ListFragment.RefreshListener());
        }

        RefreshSubscriber(Subscriber<?> subscriber, boolean shareSubscriptions) {
            super(subscriber, shareSubscriptions, new ListFragment.RefreshListener());
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
}
