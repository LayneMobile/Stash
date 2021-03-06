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
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import rx.Observable;
import rxsubscriptions.RxSubscriptions;
import rxsubscriptions.SubscriptionBuilder;
import rxsubscriptions.components.RxsFragment;
import stash.Request;
import stash.samples.hockeyloader.RxExtensions;

public class HockeyFragment extends RxsFragment implements RxExtensions {

    private HockeyFragmentListener mListener;

    @Override protected RxSubscriptions createSubscriptions() {
        return RxSubscriptions.observeUntilStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (HockeyFragmentListener) activity;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected HockeyFragmentListener getListener() {
        return mListener;
    }

    @Override public <T> Request.Extender<T> asRequest() {
        return Request.extender();
    }

    @Override public <T> SubscriptionBuilderExtension<T> asSubscriptionBuilder() {
        return new SubscriptionBuilderExtension<T>() {
            @Override public SubscriptionBuilder<T> call(Observable.OnSubscribe<T> onSubscribe) {
                return subscriptions()
                        .with(Observable.create(onSubscribe));
            }
        };
    }

    public interface HockeyFragmentListener {
        void push(HockeyFragment fragment);
    }
}
