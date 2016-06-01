
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

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public final class Constants {
    public static final ClassName NON_NULL = ClassName.get("android.support.annotation", "NonNull");
    public static final ClassName OVERRIDE = ClassName.get(Override.class);
    public static final ClassName ASSERTION_ERROR = ClassName.get(AssertionError.class);
    public static final ClassName GSON = ClassName.get("com.google.gson", "Gson");
    public static final ClassName GSON_BUILDER = ClassName.get("com.google.gson", "GsonBuilder");
    public static final ClassName TYPE_ADAPTER_FACTORY = ClassName.get("com.google.gson", "TypeAdapterFactory");
    public static final TypeName TYPE_ADAPTER_FACTORY_ARRAY = ArrayTypeName.of(TYPE_ADAPTER_FACTORY);
    public static final ClassName GSON_FACTORY = ClassName.get("stash.util.gson", "GsonFactory");

    private Constants() { throw new AssertionError("no instances"); }
}
