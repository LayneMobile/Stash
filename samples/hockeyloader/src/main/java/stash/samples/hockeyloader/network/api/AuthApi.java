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
import stash.Aggregable;
import stash.StashPolicy;
import stash.Stashable;
import stash.Stashes;
import stash.aggregables.SimpleAggregable;
import stash.StashableApi;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.Auth;
import stash.samples.hockeyloader.network.util.NetworkUtils;

public final class AuthApi {
    private static final int MEM_SIZE = 1;
    private static final String PATH = "/auth_tokens";

    interface Service {
        @GET(PATH) Auth getAuth(@Header("Authorization") String basicAuthValue);
    }

    static {
        Stashes.memDb().registerMaxSize(Auth.class, MEM_SIZE);
    }

    private AuthApi() { throw new AssertionError("no instances"); }

    static final StashableApi<Auth, Params> INSTANCE = new RetrofitStashableApiBuilder<Auth, Params, Service>()
            .buildRetrofitSource()
            .serviceType(Service.class)
            .source(new Func2<Service, Params, Auth>() {
                @Override public Auth call(Service service, Params params) {
                    return service.getAuth(((SourceParams) params).auth);
                }
            })
            .add()
            .stash(new Func1<Params, Stashable<Auth>>() {
                @Override public Stashable<Auth> call(Params params) {
                    return new Stashable.Builder<>(Auth.class, params)
                            .primary(Stashes.memDb())
                            .secondary(Stashes.gsonDb())
                            .maxAge(TimeUnit.DAYS, 1)
                            .build();
                }
            })
            .aggregate(new Func1<Params, Aggregable>() {
                @Override public Aggregable call(Params params) {
                    final String key;
                    if (params instanceof SourceParams) {
                        key = ((SourceParams) params).auth;
                    } else {
                        key = params.getKey();
                    }
                    return new SimpleAggregable(key);
                }
            })
            .build();

    interface Params extends StashableParams<String>, NetworkParams { }

    public static class StashParams implements Params {
        final String key;

        private StashParams(Builder builder) {
            this.key = builder.username;
        }

        @NonNull @Override public StashPolicy getStashPolicy() {
            return StashPolicy.STASH_ONLY_NO_SOURCE;
        }

        @Override public String getKey() {
            return key;
        }

        public static class Builder {
            String username;

            public Builder setUsername(String username) {
                this.username = username;
                return this;
            }

            public StashParams build() {
                verify();
                return new StashParams(this);
            }

            private void verify() {
                if (username == null) {
                    throw new IllegalArgumentException(
                            "username must not be null");
                }
            }
        }
    }

    public static final class SourceParams extends StashParams {
        private final StashPolicy stashPolicy;
        private final String auth;

        private SourceParams(Builder builder) {
            super(builder);
            this.stashPolicy = builder.stashPolicy;
            this.auth = NetworkUtils.toBasicAuthHeaderValue(builder.username, builder.password);
        }

        @NonNull @Override public StashPolicy getStashPolicy() {
            return stashPolicy;
        }

        public static final class Builder extends StashParams.Builder {
            private String password;
            private StashPolicy stashPolicy = StashPolicy.STASH_UNLESS_EXPIRED;

            public Builder setUsername(String username) {
                super.setUsername(username);
                return this;
            }

            public Builder setPassword(String password) {
                this.password = password;
                return this;
            }

            public Builder setStashPolicy(StashPolicy stashPolicy) {
                this.stashPolicy = stashPolicy;
                return this;
            }

            public SourceParams build() {
                verify();
                return new SourceParams(this);
            }

            private void verify() {
                super.verify();
                if (password == null) {
                    if (stashPolicy != StashPolicy.STASH_ONLY_NO_SOURCE) {
                        throw new IllegalArgumentException(
                                "password can only be null for stash-only request");
                    }
                    password = "";
                }
            }
        }
    }
}
