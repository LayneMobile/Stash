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

import stash.Params;
import stash.Progress;
import stash.Request;
import stash.RequestProcessor;
import stash.Source;
import stash.SourceProcessor;
import stash.Stashable;
import stash.StashableProcessor;

public final class RequestProcessorBuilder<T, P extends Params> {
    private DefaultRequestProcessor<T, P> processor;
    private SourceProgressProcessor<T, P> sourceProcessor;
    private StashableProcessor<T, P> stashableProcessor;

    public RequestProcessorBuilder() {}

    public void setSource(@NonNull Source<T, P> source) {
        this.processor = DefaultRequestProcessor.create(source);
    }

    public void setSourceProcessor(@NonNull SourceProcessor<T, P> sourceProcessor) {
        this.sourceProcessor = SourceProgressProcessor.create(sourceProcessor);
    }

    public void setStashableProcessor(@NonNull StashableProcessor<T, P> stashableProcessor) {
        this.stashableProcessor = stashableProcessor;
    }

    public RequestProcessor<T, P> build() {
        DefaultRequestProcessor<T, P> processor = this.processor;
        if (processor != null) {
            return new RequestProcessorImpl<T, P>(processor);
        }
        SourceProgressProcessor<T, P> sourceProcessor = this.sourceProcessor;
        StashableProcessor<T, P> stashableProcessor = this.stashableProcessor;
        if (sourceProcessor == null || stashableProcessor == null) {
            throw new IllegalStateException("not enough information specified to create request processor");
        }
        processor = DefaultRequestProcessor.create(sourceProcessor, stashableProcessor);
        return new RequestProcessorImpl<T, P>(processor);
    }

    private static final class RequestProcessorImpl<T, P extends Params> implements RequestProcessor<T, P> {
        private final DefaultRequestProcessor<T, P> impl;

        private RequestProcessorImpl(DefaultRequestProcessor<T, P> impl) {
            this.impl = impl;
        }

        @NonNull @Override public Request<Progress<T>> getProgressRequest(@NonNull P p) {
            return impl.getProgressRequest(p);
        }

        @NonNull @Override public Request<T> getRequest(@NonNull P p) {
            return impl.getRequest(p);
        }

        @Nullable @Override public Stashable<T> getStashable(@NonNull P p) {
            return impl.getStashable(p);
        }
    }
}
