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
import stash.Aggregable;
import stash.Progress;
import stash.Request;
import stash.Stash;
import stash.StashPolicy;
import stash.Stashable;
import stash.StashableApi;
import stash.Stashes;
import stash.aggregables.SimpleAggregable;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.Auth;
import stash.samples.hockeyloader.network.util.NetworkUtils;

@Value.Enclosing
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
                    return service.getAuth(((SourceParams) params).auth());
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
                        key = ((SourceParams) params).auth();
                    } else {
                        key = params.getKey();
                    }
                    return new SimpleAggregable(key);
                }
            })
            .build();

    interface Params extends StashableParams<String>, NetworkParams {
        String username();
    }

    @Value.Immutable
    public static abstract class StashParams implements Params {
        @NonNull @Override public final StashPolicy getStashPolicy() {
            return StashPolicy.STASH_ONLY_NO_SOURCE;
        }

        @Value.Derived
        @Override public String getKey() {
            return username();
        }

        public Request<Auth> request() {
            return INSTANCE.getRequest(this);
        }

        public Request<Progress<Auth>> progressRequest() {
            return INSTANCE.getProgressRequest(this);
        }

        public Stash<Auth> stash() {
            return INSTANCE.getStash(this);
        }

        public SourceParams.Builder toSourceParams() {
            return new SourceParams.Builder()
                    .from(this);
        }

        public static final class Builder extends AuthApiImpl.StashParams.Builder {
            public Request<Auth> request() {
                return build().request();
            }

            public Request<Progress<Auth>> progressRequest() {
                return build().progressRequest();
            }

            public Stash<Auth> stash() {
                return build().stash();
            }
        }
    }

    @Value.Immutable
    public static abstract class SourceParams implements Params {
        abstract String password();

        @Value.Default
        @NonNull @Override public StashPolicy getStashPolicy() {
            return StashPolicy.STASH_UNLESS_EXPIRED;
        }

        @Value.Derived
        @Override public String getKey() {
            return username();
        }

        @Value.Derived String auth() {
            return NetworkUtils.toBasicAuthHeaderValue(username(), password());
        }

        public Request<Auth> request() {
            return INSTANCE.getRequest(this);
        }

        public Request<Progress<Auth>> progressRequest() {
            return INSTANCE.getProgressRequest(this);
        }

        public Stash<Auth> stash() {
            return INSTANCE.getStash(this);
        }

        public static final class Builder extends AuthApiImpl.SourceParams.Builder {
            public Request<Auth> request() {
                return build().request();
            }

            public Request<Progress<Auth>> progressRequest() {
                return build().progressRequest();
            }

            public Stash<Auth> stash() {
                return build().stash();
            }
        }
    }
}
