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

import android.support.annotation.Nullable;

import stash.Aggregable;
import stash.Entry;

public interface OpenEndedAggregable<T> extends Aggregable {
    /**
     * Determines the delay in milliseconds until the next call to the source.
     *
     * @param entry
     *         the current entry (could be from source or from the stash)
     *
     * @return the delay until the next source should be called, or -1 in order to prevent scheduling another call
     */
    long delayUntilNextRefresh(@Nullable Entry<T> entry);
}
