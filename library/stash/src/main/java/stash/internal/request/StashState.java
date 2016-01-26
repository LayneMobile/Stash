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

package stash.internal.request;


import stash.Entry;
import stash.Metadata;

final class StashState<T> {
    final Kind kind;
    final Metadata stashMetadata;
    final T stashData;
    final Entry<T> stashEntry;

    static <T> StashState<T> stash(Metadata stashMetadata, T stashData) {
        return new StashState<T>(Kind.Stash, stashMetadata, stashData);
    }

    static <T> StashState<T> source() {
        return new StashState<T>(Kind.Source, null, null);
    }

    static <T> StashState<T> stashAndSource(Metadata stashMetadata, T stashData) {
        return new StashState<T>(Kind.StashAndSource, stashMetadata, stashData);
    }

    private StashState(Kind kind, Metadata stashMetadata, T stashData) {
        this.kind = kind;
        this.stashMetadata = stashMetadata;
        this.stashData = stashData;
        this.stashEntry = new Entry.Builder<T>()
                .setMetadata(stashMetadata)
                .setData(stashData)
                .build();
    }

    enum Kind {
        Stash,
        Source,
        StashAndSource
    }
}
