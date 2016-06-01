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

package stash.samples.hockeyloader.network.util.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.laynemobile.android.gson.GsonParcelable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import stash.samples.hockeyloader.network.util.gson.typeadapters.AbstractTypeAdapter;
import stash.util.gson.GsonFactory;

public final class GsonUtil {
    private static final GsonFactory DEFAULT_FACTORY = new Factory(GsonFactory.DEFAULT);
    private static final AtomicReference<GsonFactory> INSTANCE = new AtomicReference<>(DEFAULT_FACTORY);

    private GsonUtil() { throw new AssertionError("no instances"); }

    public static Gson gson() {
        return INSTANCE.get().gson();
    }

    public static GsonBuilder newGsonBuilder() {
        return INSTANCE.get().newGsonBuilder();
    }

    public static boolean register(GsonFactory factory) {
        if (INSTANCE.compareAndSet(DEFAULT_FACTORY, new Factory(factory))) {
            GsonParcelable.setGson(gson());
            return true;
        }
        return false;
    }

    private static final class Factory extends AtomicReference<Gson> implements GsonFactory {
        private final GsonFactory mFactory;

        private Factory(GsonFactory factory) {
            mFactory = factory;
        }

        @Override public Gson gson() {
            Gson gson;
            if ((gson = get()) == null) {
                compareAndSet(null, newGsonBuilder().create());
                return get();
            }
            return gson;
        }

        @Override public GsonBuilder newGsonBuilder() {
            return mFactory.newGsonBuilder()
                    .registerTypeAdapterFactory(new AbstractTypeAdapterFactory());
        }
    }


    private static final class AbstractTypeAdapterFactory implements TypeAdapterFactory {
        private static final List<TypeToken<?>> TOKENS = new ImmutableList.Builder<TypeToken<?>>()
                // Add interfaces that have Immutables implementation with Gson adapters
                .build();

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (TOKENS.contains(type)) {
                return new AbstractTypeAdapter<>(gson);
            }
            return null;
        }
    }
}
