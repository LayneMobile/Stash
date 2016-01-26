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


class Blueprint<T> {
    final StashRunner<T> stashRunner;
    final Steps steps;

    static <T> Blueprint<T> create(Steps steps) {
        return new Blueprint<T>(null, steps);
    }

    static <T> Blueprint<T> create(StashRunner<T> stashRunner, Steps steps) {
        return new Blueprint<T>(stashRunner, steps);
    }

    private Blueprint(StashRunner<T> stashRunner, Steps steps) {
        this.stashRunner = stashRunner;
        this.steps = steps;
    }

    enum Steps {
        None,
        StashOnly,
        SourceOnly,
        SourceAndSaveOnly,
        SourceAndSave_OnErrorStash,
        StashState
    }
}
