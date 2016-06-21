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

package stash.aggregables;

import android.support.annotation.NonNull;

import stash.Aggregable;

public class SimpleAggregable implements Aggregable {
    public static final int DEFAULT_KEEP_ALIVE_SECONDS = 10;
    public static final boolean DEFAULT_KEEP_ALIVE_ON_ERROR = false;

    @NonNull private final Object key;
    private final int keepAliveSeconds;
    private final boolean keepAliveOnError;

    public SimpleAggregable(@NonNull Object key) {
        this(key, DEFAULT_KEEP_ALIVE_SECONDS);
    }

    public SimpleAggregable(@NonNull Object key, int keepAliveSeconds) {
        this(key, keepAliveSeconds, DEFAULT_KEEP_ALIVE_ON_ERROR);
    }

    public SimpleAggregable(@NonNull Object key, int keepAliveSeconds, boolean keepAliveOnError) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        this.key = key;
        this.keepAliveSeconds = keepAliveSeconds;
        this.keepAliveOnError = keepAliveOnError;
    }

    @NonNull @Override public Object key() {
        return key;
    }

    @Override public int keepAliveSeconds() {
        return keepAliveSeconds;
    }

    @Override public boolean keepAliveOnError() {
        return keepAliveOnError;
    }
}
