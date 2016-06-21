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

package stash.schedulers;

import android.content.Context;

import java.util.concurrent.atomic.AtomicReference;

import rx.Scheduler;
import rx.schedulers.Schedulers;
import stash.internal.StashLog;
import stash.internal.StashModuleImpl;
import stash.services.StashService;

public final class StashSchedulers {
    private static final String TAG = StashSchedulers.class.getSimpleName();
    private static final AtomicReference<StashSchedulers> INSTANCE = new AtomicReference<StashSchedulers>();

    private final Scheduler service;

    private StashSchedulers() {
        Scheduler service = StashModuleImpl.getInstance().getSchedulersHook().getServiceScheduler();
        if (service != null) {
            this.service = service;
        } else {
            final Context context = StashModuleImpl.getInstance().getContext();
            if (context != null) {
                this.service = ServiceScheduler.create(context, StashService.class, Schedulers.io());
            } else {
                StashLog.e(TAG, "Module context is null. Using Schedulers.io() for service scheduler");
                this.service = Schedulers.io();
            }
        }
    }

    private static StashSchedulers getInstance() {
        StashSchedulers instance = INSTANCE.get();
        if (instance == null) {
            INSTANCE.compareAndSet(null, new StashSchedulers());
            instance = INSTANCE.get();
        }
        return instance;
    }

    public static Scheduler service() {
        return getInstance().service;
    }
}
