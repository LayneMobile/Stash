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

import java.util.concurrent.TimeUnit;

import retrofit.http.GET;
import retrofit.http.Header;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.StashPolicy;
import stash.Stashable;
import stash.Stashes;
import stash.StashableApi;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.Apps;
import stash.samples.hockeyloader.network.model.Auth;

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
                    return service.getApps(params.token);
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

    public static final class Params implements NetworkParams, StashableParams<String> {
        private final String token;
        private final StashPolicy stashPolicy;

        private Params(Builder builder) {
            this.token = builder.token;
            this.stashPolicy = builder.stashPolicy;
        }

        @NonNull @Override public StashPolicy getStashPolicy() {
            return stashPolicy;
        }

        @Override public String getKey() {
            return token;
        }

        public Builder mutate() {
            return new Builder(this);
        }

        public static final class Builder {
            private String token;
            private StashPolicy stashPolicy = StashPolicy.DEFAULT;

            public Builder() {}

            private Builder(Params params) {
                this.token = params.token;
                this.stashPolicy = params.stashPolicy;
            }

            public Builder setToken(Auth.Token token) {
                this.token = token.getToken();
                return this;
            }

            public Builder setStashPolicy(StashPolicy stashPolicy) {
                this.stashPolicy = stashPolicy;
                return this;
            }

            public Params build() {
                validate();
                return new Params(this);
            }

            private void validate() {
                if (token == null) throw new IllegalArgumentException("must set token");
            }
        }
    }
}
