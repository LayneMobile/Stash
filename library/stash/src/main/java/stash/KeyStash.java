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

public class KeyStash<K, V> extends Stash<V> {
    private final K key;

    protected KeyStash(@NonNull Stash<V> stash, @NonNull K key) {
        super(stash);
        this.key = key;
    }

    protected KeyStash(@NonNull Worker<V> worker, @NonNull K key) {
        super(worker);
        this.key = key;
    }

    public static <K, V> KeyStash<K, V> create(@NonNull Worker<V> worker, @NonNull K key) {
        return new KeyStash<K, V>(worker, key);
    }

    public static <K, V> KeyStash<K, V> create(@NonNull Stash<V> stash, @NonNull K key) {
        return new KeyStash<K, V>(stash, key);
    }

    public final K getKey() {
        return key;
    }
}
