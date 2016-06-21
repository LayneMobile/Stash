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

public final class Functions {
    private Functions() {}

    public interface StashDb<K> {
        @NonNull <T> Functions.Stash<T, K> stashFunction();

        @NonNull <T> Functions.StashCollection<T, K> stashCollectionFunction();
    }

    public interface Stash<T, K> {
        @NonNull stash.Stash<T> getStash(@NonNull Class<T> type, @NonNull StashKey<? extends K> stashKey);
    }

    public interface StashCollection<T, K> {
        @NonNull stash.StashCollection<K, T> getStashCollection(@NonNull Class<T> type);
    }
}
