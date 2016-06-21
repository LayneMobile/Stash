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

import android.support.annotation.NonNull;

import java.util.Collection;

import stash.Request;
import stash.Stash;
import stash.StashCollection;
import stash.StashDb;
import stash.StashKey;

public abstract class MemDb extends StashDb<Object> {

    public static MemDb create(@NonNull final Worker worker) {
        return new Impl(worker);
    }

    @NonNull public abstract <T> MemDb registerMaxSize(@NonNull Class<T> type, int maxSize);

    public interface Worker extends StashDb.Worker<Object> {
        <T> void registerMaxSize(@NonNull Class<T> type, int maxSize);
    }

    private static class Impl extends MemDb {
        private final Worker worker;
        private final StashDb<Object> impl;

        private Impl(Worker worker) {
            this.worker = worker;
            this.impl = StashDb.create(worker);
        }

        @NonNull @Override public <T> MemDb registerMaxSize(@NonNull Class<T> type, int maxSize) {
            worker.registerMaxSize(type, maxSize);
            return this;
        }

        @NonNull @Override public <V> StashCollection<Object, V> getStashCollection(@NonNull Class<V> type) {
            return impl.getStashCollection(type);
        }

        @NonNull @Override public <V> Stash<V> getStash(@NonNull Class<V> type, @NonNull StashKey<?> stashKey) {
            return impl.getStash(type, stashKey);
        }

        @NonNull @Override public Request<Object> keys() {
            return impl.keys();
        }

        @NonNull @Override public Request<Boolean> removeAll() {
            return impl.removeAll();
        }

        @NonNull @Override public Request<Boolean> removeAll(@NonNull Collection<StashKey<?>> stashKeys) {
            return impl.removeAll(stashKeys);
        }
    }
}
