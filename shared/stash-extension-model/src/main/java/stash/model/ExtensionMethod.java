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

package stash.model;


import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

final class ExtensionMethod {
    final ExtensionMethodKind kind;
    final ExecutableElement method;
    final List<TypeElement> returnAnnotations;

    ExtensionMethod(ExtensionMethodKind kind, ExecutableElement method, List<TypeElement> returnAnnotations) {
        if (kind == ExtensionMethodKind.Instance && method.getParameters().size() > 0) {
            throw new IllegalArgumentException("instance method cannot have parameters");
        }
        this.kind = kind;
        this.method = method;
        this.returnAnnotations = Collections.unmodifiableList(returnAnnotations);
    }

    String name() {
        return method.getSimpleName().toString();
    }
}
