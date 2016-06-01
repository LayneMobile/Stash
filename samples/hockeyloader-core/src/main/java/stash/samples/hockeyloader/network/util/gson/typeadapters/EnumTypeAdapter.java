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

package stash.samples.hockeyloader.network.util.gson.typeadapters;

import android.support.annotation.NonNull;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EnumTypeAdapter<E extends Enum<E>> extends TypeAdapter<E> {
    private final AtomicReference<E> mUnknownValue = new AtomicReference<>();

    @NonNull
    protected abstract E unknownValue();

    @NonNull
    protected abstract E[] values();

    @Override
    public E read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return unknownValueInternal();
        }
        String name = in.nextString();
        for (E e : values()) {
            if (name.equalsIgnoreCase(e.name())) {
                return e;
            }
        }
        return unknownValueInternal();
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        E v = value == null ? unknownValueInternal() : value;
        out.value(v.name());
    }

    @NonNull
    private E unknownValueInternal() {
        E unknownValue = mUnknownValue.get();
        if (unknownValue == null) {
            mUnknownValue.compareAndSet(null, unknownValue());
            return mUnknownValue.get();
        }
        return unknownValue;
    }
}
