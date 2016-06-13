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

package stash.internal;

import android.support.annotation.NonNull;

import sourcerer.ExtensionMethod;
import sourcerer.ExtensionMethod.Kind;
import stash.Stash;
import stash.StashCollection;
import stash.StashKey;
import stash.annotations.Stashes;
import stash.stashdbs.MemDb;

@Stashes
public final class StashesImpl {
    private static final StashesImpl INSTANCE = new StashesImpl();

    private final MemDb memDb;

    private StashesImpl() {
        MemDb mem = StashModuleImpl.getInstance().getMemDbHook().getMemDb();
        if (mem == null) {
            mem = MemDbImpl.create();
        }
        this.memDb = mem;
    }

    @ExtensionMethod(Kind.Instance) @NonNull public static StashesImpl getInstance() {
        return INSTANCE;
    }

    @ExtensionMethod(Kind.Return) @NonNull public MemDb memDb() {
        return memDb;
    }

    @ExtensionMethod(Kind.Return) @NonNull public <T> StashCollection<Object, T> memCollection(@NonNull Class<T> type) {
        return memDb.getStashCollection(type);
    }

    @ExtensionMethod(Kind.Return) @NonNull
    public <T> Stash<T> mem(@NonNull Class<T> type, @NonNull StashKey<?> stashKey) {
        return memDb.getStash(type, stashKey);
    }
}
