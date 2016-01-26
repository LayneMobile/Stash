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

import rx.Observable;
import stash.internal.request.SourceProcessorBuilder;

public interface SourceProcessor<T, P extends Params> {
    @NonNull Observable<T> getSourceRequest(@NonNull P p);

    @Nullable Observable<T> peekSourceRequest(@NonNull P p);

    final class Builder<T, P extends Params> implements stash.Builder<SourceProcessor<T, P>> {
        private final SourceProcessorBuilder<T, P> builder = new SourceProcessorBuilder<T, P>();

        @NonNull public Builder<T, P> setSource(@NonNull Source<T, P> source) {
            builder.setSource(source);
            return this;
        }

        @NonNull @Override public SourceProcessor<T, P> build() {
            return builder.build();
        }
    }
}
