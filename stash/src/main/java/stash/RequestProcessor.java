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

import stash.internal.request.RequestProcessorBuilder;

public interface RequestProcessor<T, P extends Params> extends StashableProcessor<T, P> {
    @NonNull Request<T> getRequest(@NonNull P p);

    @NonNull Request<Progress<T>> getProgressRequest(@NonNull P p);

    final class Builder<T, P extends Params> implements stash.Builder<RequestProcessor<T, P>> {
        private final RequestProcessorBuilder<T, P> impl = new RequestProcessorBuilder<T, P>();

        public Builder<T, P> setSource(@NonNull Source<T, P> source) {
            impl.setSource(source);
            return this;
        }

        public Builder<T, P> setSourceProcessor(@NonNull SourceProcessor<T, P> sourceProcessor) {
            impl.setSourceProcessor(sourceProcessor);
            return this;
        }

        public Builder<T, P> setStashableProcessor(@NonNull StashableProcessor<T, P> stashableProcessor) {
            impl.setStashableProcessor(stashableProcessor);
            return this;
        }

        @NonNull @Override public RequestProcessor<T, P> build() {
            return impl.build();
        }
    }
}
