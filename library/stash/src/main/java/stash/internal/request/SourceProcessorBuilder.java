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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import stash.Params;
import stash.Source;
import stash.SourceProcessor;

public final class SourceProcessorBuilder<T, P extends Params> {
    private Source<T, P> source;

    public void setSource(@NonNull Source<T, P> source) {
        this.source = source;
    }

    @NonNull public SourceProcessor<T, P> build() {
        if (source == null) {
            throw new IllegalStateException("source must not be null");
        }
        return new SourceProcessorImpl<T, P>(DefaultSourceProcessor.create(source));
    }

    private static final class SourceProcessorImpl<T, P extends Params> implements SourceProcessor<T, P> {
        private final DefaultSourceProcessor<T, P> impl;

        private SourceProcessorImpl(DefaultSourceProcessor<T, P> impl) {
            this.impl = impl;
        }

        @NonNull @Override public Observable<T> getSourceRequest(@NonNull P p) {
            return impl.getSourceRequest(p);
        }

        @Nullable @Override public Observable<T> peekSourceRequest(@NonNull P p) {
            return impl.peekSourceRequest(p);
        }
    }
}
