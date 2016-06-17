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
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import stash.StashPolicy;
import stash.exceptions.StashBaseException;
import stash.internal.StashLog;
import stash.samples.hockeyloader.HockeyIntent;
import stash.samples.hockeyloader.R;
import stash.samples.hockeyloader.network.api.Api;
import stash.samples.hockeyloader.network.api.AppVersionsApi;
import stash.samples.hockeyloader.network.model.App;
import stash.samples.hockeyloader.network.model.AppVersion;
import stash.samples.hockeyloader.network.model.AppVersions;
import stash.samples.hockeyloader.network.model.Auth;

public class AppVersionsFragment extends ListFragment {
    private static final String TAG = AppVersionsFragment.class.getSimpleName();

    private Auth.Token mToken;
    private App mApp;
    private AppVersions mAppVersions;
    private AppAdapter mAdapter;

    public static AppVersionsFragment newInstance(Auth.Token token, App app) {
        AppVersionsFragment fragment = new AppVersionsFragment();
        Bundle args = new Bundle();
        args.putParcelable(HockeyIntent.EXTRA_TOKEN, token);
        args.putParcelable(HockeyIntent.EXTRA_APP, app);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mToken = args.getParcelable(HockeyIntent.EXTRA_TOKEN);
        mApp = args.getParcelable(HockeyIntent.EXTRA_APP);
        mAdapter = new AppAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_apps_fragment, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setAdapter(mAdapter);
    }

    @Override protected void refresh(StashPolicy stashPolicy) {
        AppVersionsApi.Params params = new AppVersionsApi.Params.Builder()
                .setToken(mToken)
                .setApp(mApp)
                .setStashPolicy(stashPolicy)
                .build();
        Api.appVersions().getProgressRequest(params)
                .onMain(subscriptions())
                .subscribe(new Subscriber());
    }

    private class Subscriber extends RefreshSubscriber<AppVersions> {
        @Override protected void onNextData(AppVersions appVersions) {
            if (appVersions != null) {
                mAppVersions = appVersions;
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override protected void onError(StashBaseException e) {
            StashLog.e(TAG, "error", e);
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private class AppAdapter extends BaseAdapter {
        List<AppVersion> versions;

        @Override
        public int getCount() {
            AppVersions appVersions = mAppVersions;
            List<AppVersion> v = appVersions == null ? Collections.<AppVersion>emptyList() : appVersions.getAppVersions();
            versions = v;
            return v.size();
        }

        @Override
        public AppVersion getItem(int position) {
            return versions.get(position);
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

            AppVersion appVersion = getItem(position);
            view.setText(appVersion.getShortVersion() + " (" + appVersion.getVersion() + ")");

            return view;
        }
    }
}
