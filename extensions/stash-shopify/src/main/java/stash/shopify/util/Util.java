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

package stash.shopify.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Util {
    private Util() { throw new AssertionError("no instances"); }

    @Nullable public static <T> T getFirst(@Nullable List<T> list) {
        return list != null && list.size() > 0
                ? list.get(0)
                : null;
    }

    public static int getSize(@Nullable Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    @NonNull public static <T> List<T> nullSafe(@Nullable List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }
}
