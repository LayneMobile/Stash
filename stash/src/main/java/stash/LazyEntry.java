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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import stash.annotations.Keep;
import stash.internal.StashLog;
import stash.internal.Util;

public abstract class LazyEntry<T> implements Entry<T>, Closeable {
    private static final String TAG = LazyEntry.class.getSimpleName();
    private static final AtomicIntegerFieldUpdater<LazyEntry> CLOSED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(LazyEntry.class, "closed");

    private final Object metaLock = new Object();
    private final Object dataLock = new Object();
    private volatile Metadata metadata;
    private volatile boolean metadataLoaded;
    private volatile T data;
    private volatile boolean dataLoaded;
    @Keep private volatile int closed;

    protected LazyEntry() {}

    @Nullable public static <T> LazyEntry<T> cast(@Nullable Entry<T> entry) {
        if (entry instanceof LazyEntry) {
            return (LazyEntry<T>) entry;
        } else if (entry != null) {
            return new LazyEntryWrapper<T>(entry);
        }
        return null;
    }

    @NonNull protected abstract Metadata loadMetadata();

    @Nullable protected abstract T loadData();

    protected abstract void closeInternal() throws IOException;

    @NonNull @Override public final Metadata getMetadata() {
        if (!metadataLoaded) {
            synchronized (metaLock) {
                if (!metadataLoaded) {
                    try {
                        metadata = loadMetadata();
                    } catch (Throwable e) {
                        StashLog.e(TAG, "Error loading metadata", e);
                    }
                    metadataLoaded = true;
                }
            }
            tryClose();
        }
        return metadata == null ? Metadata.EMPTY : metadata;
    }

    @Nullable @Override public final T getData() {
        if (!dataLoaded) {
            synchronized (dataLock) {
                if (!dataLoaded) {
                    try {
                        data = loadData();
                    } catch (Throwable e) {
                        StashLog.e(TAG, "Error loading data", e);
                    }
                    dataLoaded = true;
                }
            }
            tryClose();
        }
        return data;
    }

    @Override public final void close() throws IOException {
        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            closeInternal();
        }
    }

    public final boolean isClosed() {
        return closed != 0;
    }

    public final void closeQuietly() {
        Util.closeQuietly(this);
    }

    private void tryClose() {
        if (metadataLoaded && dataLoaded) {
            closeQuietly();
        }
    }

    private static final class LazyEntryWrapper<T> extends LazyEntry<T> {
        private final Entry<T> entry;

        private LazyEntryWrapper(@NonNull Entry<T> entry) {
            this.entry = entry;
        }

        @NonNull @Override protected Metadata loadMetadata() {
            return entry.getMetadata();
        }

        @Nullable @Override protected T loadData() {
            return entry.getData();
        }

        @Override protected void closeInternal() throws IOException {
            Util.close(entry);
        }
    }
}
