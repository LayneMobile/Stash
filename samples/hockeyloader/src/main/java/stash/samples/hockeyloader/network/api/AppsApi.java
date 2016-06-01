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

package stash.samples.hockeyloader.network.api;


import android.support.annotation.NonNull;

import org.immutables.value.Value;

import java.util.concurrent.TimeUnit;

import retrofit.http.GET;
import retrofit.http.Header;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.Progress;
import stash.Request;
import stash.Stash;
import stash.StashPolicy;
import stash.Stashable;
import stash.StashableApi;
import stash.Stashes;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.Apps;
import stash.samples.hockeyloader.network.model.Auth;

@Value.Enclosing
public final class AppsApi {
    private interface Service {
        String PATH = "/apps";

        @GET(PATH) Apps getApps(@Header("X-HockeyAppToken") String authToken);
    }

    private AppsApi() { throw new AssertionError("no instances"); }

    static final StashableApi<Apps, Params> INSTANCE = new RetrofitStashableApiBuilder<Apps, Params, Service>()
            .buildRetrofitSource()
            .serviceType(Service.class)
            .source(new Func2<Service, Params, Apps>() {
                @Override public Apps call(Service service, Params params) {
                    return service.getApps(params.getKey());
                }
            })
            .add()
            .stash(new Func1<Params, Stashable<Apps>>() {
                @Override public Stashable<Apps> call(Params params) {
                    return new Stashable.Builder<>(Apps.class, params)
                            .primary(Stashes.gsonDb())
                            .maxAge(TimeUnit.DAYS, 1)
                            .build();
                }
            })
            .aggregate()
            .build();

    @Value.Immutable
    public static abstract class Params implements NetworkParams, StashableParams<String> {
        abstract Auth.Token token();

        @Value.Default
        @NonNull
        @Override public StashPolicy getStashPolicy() {
            return StashPolicy.DEFAULT;
        }

        @Value.Derived
        @Override public String getKey() {
            return token().getToken();
        }

        public Builder mutate() {
            return new Builder()
                    .from(this);
        }

        public Request<Apps> request() {
            return INSTANCE.getRequest(this);
        }

        public Request<Progress<Apps>> progressRequest() {
            return INSTANCE.getProgressRequest(this);
        }

        public Stash<Apps> stash() {
            return INSTANCE.getStash(this);
        }

        public static final class Builder extends AppsApiImpl.Params.Builder {
            public Request<Apps> request() {
                return build().request();
            }

            public Request<Progress<Apps>> progressRequest() {
                return build().progressRequest();
            }

            public Stash<Apps> stash() {
                return build().stash();
            }
        }
    }
}
