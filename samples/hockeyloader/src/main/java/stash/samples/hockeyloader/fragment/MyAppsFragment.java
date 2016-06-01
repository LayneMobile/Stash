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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import stash.StashPolicy;
import stash.exceptions.StashBaseException;
import stash.internal.StashLog;
import stash.samples.hockeyloader.R;
import stash.samples.hockeyloader.network.api.AppsApi;
import stash.samples.hockeyloader.network.model.App;
import stash.samples.hockeyloader.network.model.Apps;
import stash.samples.hockeyloader.network.model.Auth;

public class MyAppsFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = MyAppsFragment.class.getSimpleName();

    private Adapter adapter;
    private Auth.Token token;
    private Apps apps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new Adapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_apps_fragment, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        App app = adapter.getItem(position);
        if (app != null) {
            getListener().push(AppVersionsFragment.newInstance(token, app));
        }
    }

    @Override protected void refresh(StashPolicy stashPolicy) {
        AppsApi.Params params = getParams(stashPolicy);
        if (params == null) {
            swipey.setRefreshing(false);
            return;
        }
        params.progressRequest()
                .onMain(subscriptions())
                .subscribe(new Subscriber());
    }

    private AppsApi.Params getParams(StashPolicy stashPolicy) {
        if (token == null) {
            Auth auth = Auth.getCurrentUser();
            if (auth != null) {
                getActivity().setTitle(auth.getName());
                for (Auth.Token test : auth.getTokens()) {
                    if (test.getAppId() == 0) {
                        token = test;
                        break;
                    }
                }
            }
        }
        if (token != null) {
            return new AppsApi.Params.Builder()
                    .setToken(token)
                    .setStashPolicy(stashPolicy)
                    .build();
        }
        return null;
    }

    private class Subscriber extends RefreshSubscriber<Apps> {
        @Override protected void onNextData(Apps apps) {
            StashLog.d(TAG, "apps: %s", apps);
            if (apps != null) {
                MyAppsFragment.this.apps = apps;
                adapter.notifyDataSetChanged();
            }
        }

        @Override protected void onError(StashBaseException e) {
            Toast.makeText(getActivity(), "failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class Adapter extends BaseAdapter {
        List<App> _apps;

        @Override
        public int getCount() {
            Apps a = apps;
            List<App> list = a == null ? Collections.<App>emptyList() : a.getApps();
            _apps = list;
            return list.size();
        }

        @Override
        public App getItem(int position) {
            return _apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view;
            if (convertView == null) {
                view = (TextView) getActivity()
                        .getLayoutInflater()
                        .inflate(R.layout.my_apps_list_item, parent, false);
            } else {
                view = (TextView) convertView;
            }

            App app = getItem(position);
            view.setText(app.getTitle());

            return view;
        }
    }
}
