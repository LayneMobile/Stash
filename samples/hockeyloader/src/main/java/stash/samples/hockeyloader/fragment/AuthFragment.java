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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import rx.functions.Action0;
import rx.functions.Action1;
import stash.StashPolicy;
import stash.internal.StashLog;
import stash.samples.hockeyloader.R;
import stash.samples.hockeyloader.network.api.AuthApi;
import stash.samples.hockeyloader.network.model.Auth;

public class AuthFragment extends HockeyFragment {
    private static final String TAG = AuthFragment.class.getSimpleName();

    private AuthDelegate authDelegate;
    @Bind(R.id.email) EditText email;
    @Bind(R.id.password) EditText password;
    @Bind(R.id.submit) Button submit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        authDelegate = (AuthDelegate) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        authDelegate = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String lastUsername = Auth.getLastUsername(getActivity());
        if (lastUsername != null) {
            new AuthApi.StashParams.Builder()
                    .setUsername(lastUsername)
                    .request()
                    .onMain(subscriptions()).subscribe(new Action1<Auth>() {
                @Override public void call(Auth auth) {
                    if (auth != null) {
                        onSuccess(auth);
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        email.setText(Auth.getLastUsername(getActivity()));
        submit.setOnClickListener(new SubmitClick());
    }

    private void onSuccess(Auth auth) {
        Log.d(TAG, "auth: " + auth);
        Auth.setCurrentUser(auth);
        authDelegate.onAuthenticated(auth);
    }

    public interface AuthDelegate {
        void onAuthenticated(Auth auth);
    }

    private class SubmitClick implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            Editable emailText = email.getText();
            Editable passwordText = password.getText();
            if (emailText.length() == 0 || passwordText.length() == 0) {
                Toast.makeText(getActivity(), "not enough info", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            // Submit
            final String username = emailText.toString();
            final String password = passwordText.toString();
            v.setEnabled(false);
            final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
                    "",
                    getString(R.string.Authenticating),
                    true);
            new AuthApi.SourceParams.Builder()
                    .setUsername(username)
                    .setPassword(password)
                    .setStashPolicy(StashPolicy.SOURCE)
                    .request().onMain(subscriptions()).subscribe(new Action1<Auth>() {
                @Override
                public void call(Auth o) {
                    if (o != null) {
                        Auth.setLastUsername(getActivity(), username);
                        onSuccess(o);
                        progressDialog.dismiss();
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    StashLog.e("Auth", "error authenticating", throwable);
                    v.setEnabled(true);
                    progressDialog.dismiss();
                    Auth.setCurrentUser(null);
                }
            }, new Action0() {
                @Override
                public void call() {
                    v.setEnabled(true);
                    progressDialog.dismiss();
                }
            });
        }
    }
}
