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

package stash.stashdbs;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class GsonDb {

    private GsonDb() { throw new AssertionError("no instances"); }

    public static FileDb open(Config config) throws IOException {
        return FileDb.open(config.fileConfig, new Converter(config.gson));
    }

    public static Gson defaultGson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    public static class Config {
        private final FileCache.Config fileConfig;
        private final Gson gson;

        public Config(Gson gson, File dir, int version, int size) {
            this(gson, new FileCache.Config(dir, version, size));
        }

        public Config(Gson gson, FileCache.Config fileConfig) {
            if (gson == null) {
                throw new IllegalStateException("Gson cannot be null in Config");
            }
            this.fileConfig = fileConfig;
            this.gson = gson;
        }

        public static Config getDefault(Context context) {
            return new Config(defaultGson(), FileCache.Config.getDefault(context));
        }
    }

    private static class Converter implements FileDb.Converter {
        private final Gson gson;

        private Converter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public <T> T fromFile(Class<T> clazz, InputStream in) throws IOException {
            final String json = read(in);
            return gson.fromJson(json, clazz);
        }

        @Override
        public <T> void toFile(OutputStream out, T data) throws IOException {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            try {
                writer.write(gson.toJson(data));
            } finally {
                writer.close();
            }
        }

        private static String read(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            try {
                String line;
                StringBuilder str = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    // Not writing any line ending, but should be fine
                    str.append(line);
                }
                return str.toString();
            } finally {
                reader.close();
            }
        }
    }
}
