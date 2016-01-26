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

package stash.samples.hockeyloader.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import rxsubscriptions.components.support.RxsAppCompatActivity;
import stash.samples.hockeyloader.R;
import stash.samples.hockeyloader.fragment.AuthFragment;
import stash.samples.hockeyloader.fragment.HockeyFragment;
import stash.samples.hockeyloader.fragment.MyAppsFragment;
import stash.samples.hockeyloader.network.model.Auth;


public class MainActivity extends RxsAppCompatActivity
        implements AuthFragment.AuthDelegate, HockeyFragment.HockeyFragmentListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null && Auth.getCurrentUser() == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new AuthFragment())
                    .commit();
        } else if (Auth.getCurrentUser() == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new AuthFragment())
                    .commit();
        } else if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new MyAppsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the source bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle source bar item clicks here. The source bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthenticated(Auth auth) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new MyAppsFragment())
                .commit();
    }

    @Override
    public void push(HockeyFragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
