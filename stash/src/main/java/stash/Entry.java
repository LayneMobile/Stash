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

package stash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import stash.internal.Util;

public interface Entry<T> {
    @NonNull Metadata getMetadata();

    @Nullable T getData();

    final class Builder<T> implements stash.Builder<Entry<T>> {
        private final Metadata.Builder metadata = new Metadata.Builder();
        private T data;

        public Builder() {}

        public Builder(Entry<T> entry) {
            try {
                setMetadata(entry.getMetadata());
                this.data = entry.getData();
            } finally {
                Util.closeQuietly(entry);
            }
        }

        public static <T> Entry<T> copy(Entry<T> entry) {
            return new Builder<T>(entry).build();
        }

        public Builder<T> setMetadata(long lastUpdated) {
            metadata.setLastUpdated(lastUpdated);
            return this;
        }

        public Builder<T> setMetadata(List<String> metadata) {
            this.metadata.setData(metadata);
            return this;
        }

        public Builder<T> setMetadata(long lastUpdated, List<String> metadata) {
            this.metadata.setLastUpdated(lastUpdated)
                    .setData(metadata);
            return this;
        }

        public Builder<T> setMetadata(Metadata metadata) {
            if (metadata == null) { return this; }
            return setMetadata(metadata.getLastUpdated(), metadata.getData());
        }

        public Builder<T> setData(T data) {
            this.data = data;
            return this;
        }

        @NonNull @Override public Entry<T> build() {
            return new EntryImpl<T>(this);
        }

        private static final class EntryImpl<T> implements Entry<T> {
            private final Metadata metadata;
            private final T data;

            private EntryImpl(Builder<T> builder) {
                this.metadata = builder.metadata.build();
                this.data = builder.data;
            }

            @Nullable @Override public T getData() {
                return data;
            }

            @NonNull @Override public Metadata getMetadata() {
                return metadata;
            }
        }
    }
}
