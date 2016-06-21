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

import java.util.Collections;
import java.util.List;

import stash.internal.Util;

public interface Metadata {
    long getLastUpdated();

    @NonNull List<String> getData();

    Metadata EMPTY = new Builder().setLastUpdated(0).build();

    final class Builder implements stash.Builder<Metadata> {
        boolean called;
        private long lastUpdated = System.currentTimeMillis();
        private List<String> data;

        public Builder setLastUpdated(long lastUpdated) {
            called = true;
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setData(List<String> data) {
            called = true;
            this.data = data;
            return this;
        }

        @NonNull @Override public Metadata build() {
            if (!called) {
                return Metadata.EMPTY;
            }
            return new MetadataImpl(this);
        }

        private static final class MetadataImpl implements Metadata {
            private final long lastUpdated;
            private final List<String> data;

            private MetadataImpl(Builder builder) {
                this.lastUpdated = builder.lastUpdated;
                this.data = Collections.unmodifiableList(Util.nullSafe(builder.data));
            }

            @NonNull @Override public List<String> getData() {
                return data;
            }

            @Override public long getLastUpdated() {
                return lastUpdated;
            }
        }
    }
}
