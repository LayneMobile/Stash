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
import stash.Metadata;

public abstract class MetadataPredicate<T> implements EntryPredicate<T> {
    protected abstract boolean isExpired(@NonNull Metadata metadata);

    @Override public final boolean isExpired(@Nullable Entry<T> entry) {
        Metadata metadata;
        return entry == null
                || (metadata = entry.getMetadata()) == null
                || isExpired(metadata);
    }
}
