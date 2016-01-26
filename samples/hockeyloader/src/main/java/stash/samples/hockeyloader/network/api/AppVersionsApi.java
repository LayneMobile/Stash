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
import retrofit.http.Path;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.StashPolicy;
import stash.Stashable;
import stash.Stashes;
import stash.StashableApi;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.App;
import stash.samples.hockeyloader.network.model.AppVersions;
import stash.samples.hockeyloader.network.model.Auth;


public final class AppVersionsApi {
    private static final int MEM_SIZE = 2;
    private static final String PATH = "/apps/{APP_ID}/app_versions";

    interface Service {
        @GET(PATH) AppVersions getAppVersions(
                @Header("X-HockeyAppToken") String authToken,
                @Path("APP_ID") String appId);
    }

    static {
        Stashes.memDb().registerMaxSize(AppVersions.class, MEM_SIZE);
    }

    private AppVersionsApi() { throw new AssertionError("no instances"); }

    static final StashableApi<AppVersions, Params> INSTANCE = new RetrofitStashableApiBuilder<AppVersions, Params, Service>()
            .buildRetrofitSource()
            .serviceType(Service.class)
            .source(new Func2<Service, Params, AppVersions>() {
                @Override public AppVersions call(Service service, Params params) {
                    String token = params.token;
                    if (token == null) {
                        // TODO: any way to get current token?
                    }
                    return service.getAppVersions(token, params.appId);
                }
            })
            .add()
            .stash(new Func1<Params, Stashable<AppVersions>>() {
                @Override public Stashable<AppVersions> call(Params params) {
                    return new Stashable.Builder<>(AppVersions.class, params)
                            .primary(Stashes.memDb())
                            .secondary(Stashes.gsonDb())
                            .maxAge(TimeUnit.HOURS, 1)
                            .build();
                }
            })
            .aggregate()
            .build();

    public static final class Params implements NetworkParams, StashableParams<String> {
        private final String token;
        private final String appId;
        private final StashPolicy stashPolicy;

        private Params(Builder builder) {
            this.token = builder.token == null ? null : builder.token.getToken();
            this.appId = builder.app.getPublicIdentifier();
            this.stashPolicy = builder.stashPolicy;
        }

        @Override public String getKey() {
            return appId;
        }

        @NonNull @Override public StashPolicy getStashPolicy() {
            return stashPolicy;
        }

        public static final class Builder {
            private StashPolicy stashPolicy = StashPolicy.DEFAULT;
            private Auth.Token token;
            private App app;

            public Builder setStashPolicy(StashPolicy stashPolicy) {
                this.stashPolicy = stashPolicy;
                return this;
            }

            public Builder setToken(Auth.Token token) {
                this.token = token;
                return this;
            }

            public Builder setApp(App app) {
                this.app = app;
                return this;
            }

            public Params build() {
                verify();
                return new Params(this);
            }

            private void verify() {
                if (app == null) {
                    throw new IllegalArgumentException("app must not be null");
                }
            }
        }
    }
}
