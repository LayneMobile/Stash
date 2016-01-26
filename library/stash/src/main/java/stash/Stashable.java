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
import java.util.concurrent.TimeUnit;

import rx.functions.Func1;
import stash.predicates.EntryPredicate;
import stash.predicates.Predicates;


public interface Stashable<T> {
    @Nullable Stash<T> getStash();

    @Nullable EntryPredicate<T> isExpired();

    @Nullable List<String> getMetadataToSave(T t);

    final class Builder<T, K> {
        private final Class<T> type;
        private final StashKey<? extends K> stashKey;
        private Functions.Stash<T, ? super K> primary;
        private Functions.Stash<T, ? super K> secondary;
        private EntryPredicate<T> isExpired;
        private Func1<T, List<String>> metadata;

        public Builder(@NonNull Class<T> type, @NonNull StashKey<? extends K> stashKey) {
            this.type = type;
            this.stashKey = stashKey;
        }

        public Builder<T, K> primary(@NonNull StashDb<? super K> primary) {
            return primary(primary.<T>stashFunction());
        }

        public Builder<T, K> primary(@NonNull Functions.Stash<T, ? super K> primary) {
            this.primary = primary;
            return this;
        }

        public Builder<T, K> secondary(@NonNull StashDb<? super K> secondary) {
            return secondary(secondary.<T>stashFunction());
        }

        public Builder<T, K> secondary(@NonNull Functions.Stash<T, ? super K> secondary) {
            this.secondary = secondary;
            return this;
        }

        public Builder<T, K> expired(@NonNull EntryPredicate<T> isExpired) {
            this.isExpired = isExpired;
            return this;
        }

        public Builder<T, K> maxAge(TimeUnit unit, long duration) {
            this.isExpired = Predicates.maxAge(unit.toMillis(duration));
            return this;
        }

        public Builder<T, K> metadataToSave(Func1<T, List<String>> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Stashable<T> build() {
            if (primary == null) {
                throw new IllegalArgumentException("primary must not be null");
            }
            return new StashableImpl<T, K>(this);
        }

        private static final class StashableImpl<T, K> implements Stashable<T> {
            private final Class<T> type;
            private final StashKey<? extends K> stashKey;
            private final Functions.Stash<T, ? super K> primary;
            private final Functions.Stash<T, ? super K> secondary;
            private final EntryPredicate<T> isExpired;
            private final Func1<T, List<String>> metadata;

            private StashableImpl(Builder<T, K> builder) {
                this.type = builder.type;
                this.stashKey = builder.stashKey;
                this.primary = builder.primary;
                this.secondary = builder.secondary;
                this.isExpired = builder.isExpired;
                this.metadata = builder.metadata;
            }

            @Nullable @Override public Stash<T> getStash() {
                Stash<T> stash = getStash(primary, type, stashKey);
                if (secondary != null) {
                    Stash<T> second = getStash(secondary, type, stashKey);
                    return stash.combine(second);
                }
                return stash;
            }

            @Nullable @Override public EntryPredicate<T> isExpired() {
                return isExpired;
            }

            @Nullable @Override public List<String> getMetadataToSave(T t) {
                if (metadata != null) {
                    return metadata.call(t);
                }
                return null;
            }

            private static <T, K> Stash<T> getStash(Functions.Stash<T, K> stashDb, Class<T> type,
                    StashKey<? extends K> stashKey) {
                return stashDb.getStash(type, stashKey);
            }
        }
    }
}
