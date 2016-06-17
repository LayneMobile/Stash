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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/** Adapter for abstract classes and interfaces that need to store their complete subclass information. */
public class AbstractTypeAdapter<T> extends TypeAdapter<T> {
    private final Gson gson;

    public AbstractTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, T t) throws IOException {
        if (t == null) {
            out.nullValue();
        } else {
            out.beginObject();
            out.name("class");
            out.value(t.getClass().getName());
            out.name("item");
            write(out, gson.getAdapter(t.getClass()), t);
            out.endObject();
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        T t = null;
        AtomicReference<String> className = new AtomicReference<>();
        in.beginObject();
        while (in.hasNext()) {
            T test = eachAttribute(in, className);
            if (test != null) {
                t = test;
            }
        }
        in.endObject();
        return t;
    }

    private T eachAttribute(JsonReader in, AtomicReference<String> classname) throws IOException {
        String attributeName = in.nextName();
        switch (attributeName.charAt(0)) {
            case 'c':
                if ("class".equals(attributeName)) {
                    classname.set(in.nextString());
                    return null;
                }
                break;
            case 'i':
                if ("item".equals(attributeName) && classname.get() != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends T> clazz
                                = (Class<? extends T>) Class.forName(classname.get());
                        TypeAdapter<? extends T> typeAdapter = gson.getAdapter(clazz);
                        return typeAdapter.read(in);
                    } catch (Exception e) {
                        // ignore
                    }
                }
                break;
            default:
        }
        in.skipValue();
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> void write(JsonWriter out, TypeAdapter<T> itemTypeAdapter, Object value) throws IOException {
        itemTypeAdapter.write(out, (T) value);
    }
}
