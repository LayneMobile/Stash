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
import android.support.annotation.Nullable;

import org.immutables.value.Value;

import java.util.concurrent.TimeUnit;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import rx.functions.Func1;
import rx.functions.Func2;
import stash.StashPolicy;
import stash.Stashable;
import stash.StashableApi;
import stash.Stashes;
import stash.params.NetworkParams;
import stash.params.StashableParams;
import stash.retrofit.RetrofitStashableApiBuilder;
import stash.samples.hockeyloader.network.model.App;
import stash.samples.hockeyloader.network.model.AppVersions;
import stash.samples.hockeyloader.network.model.Auth;

@Value.Enclosing
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
                    String token = params.authToken();
                    if (token == null) {
                        // TODO: any way to get current token?
                    }
                    return service.getAppVersions(token, params.getKey());
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

    @Value.Immutable
    public static abstract class Params implements NetworkParams, StashableParams<String> {
        @Nullable abstract Auth.Token token();

        abstract App app();

        @Value.Default
        @NonNull @Override public StashPolicy getStashPolicy() {
            return StashPolicy.DEFAULT;
        }

        @Value.Derived
        @Override public String getKey() {
            return app().getPublicIdentifier();
        }

        @Nullable
        @Value.Derived String authToken() {
            Auth.Token token = token();
            return token == null ? null : token.getToken();
        }

        public static final class Builder extends AppVersionsApiImpl.Params.Builder {}
    }
}
