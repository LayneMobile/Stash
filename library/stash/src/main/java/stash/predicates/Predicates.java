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

package stash.predicates;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import stash.Entry;

public final class Predicates {
    private static final EntryPredicate<Object> ALWAYS_EXPIRED = new EntryPredicate<Object>() {
        @Override public boolean isExpired(@Nullable Entry<Object> entry) {
            return true;
        }
    };
    private static final EntryPredicate<Object> NEVER_EXPIRED = new EntryPredicate<Object>() {
        @Override public boolean isExpired(@Nullable Entry<Object> entry) {
            return false;
        }
    };

    @SuppressWarnings("unchecked")
    @NonNull public static <T> EntryPredicate<T> alwaysExpired() {
        return (EntryPredicate<T>) ALWAYS_EXPIRED;
    }

    @SuppressWarnings("unchecked")
    @NonNull public static <T> EntryPredicate<T> neverExpired() {
        return (EntryPredicate<T>) NEVER_EXPIRED;
    }

    @NonNull public static <T> EntryPredicate<T> maxAge(long maxAgeMillis) {
        return new LastUpdatedPredicate<T>(maxAgeMillis);
    }

    private Predicates() { throw new AssertionError("no instances"); }
}
