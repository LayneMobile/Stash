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

package stash.gson;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

public class ClassBuilder {
    public final String packageName;
    public final String className;
    public final String qualifiedName;

    public ClassBuilder(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = packageName + "." + className;
    }

    public TypeSpec.Builder newSpecBuilder() {
        return TypeSpec.classBuilder(className);
    }

    public ClassName className() {
        return ClassName.get(packageName, className);
    }

    public String javaPackagePath() {
        return packageName.replace('.', '/');
    }

    public String javaFileName() {
        return className + ".java";
    }
}
