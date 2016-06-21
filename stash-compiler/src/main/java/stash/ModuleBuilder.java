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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import stash.annotations.SourceModule;
import stash.internal.StashLog;

import static stash.Constants.NonNull;
import static stash.internal.Util.nullSafe;

final class ModuleBuilder {
    private static final String TAG = ModuleBuilder.class.getSimpleName();

    private final ApiBuilder apiBuilder;
    private final TypeName moduleType;
    private final ClassName sourceType;
    private final Types typeUtils;
    private final ExecutableElement buildMethod;
    private final Map<String, List<ExecutableElement>> methods;
    private final List<String> orderedMethodNames;
    private final boolean simple;

    ModuleBuilder(ApiBuilder apiBuilder, TypeElement module, Types typeUtils) {
        this.apiBuilder = apiBuilder;
        this.typeUtils = typeUtils;
        List<TypeVariableName> typeParams = Util.parseTypeParams(module);
        ClassName moduleClass = ClassName.get(module);
        this.moduleType = Util.coallesceParamType(moduleClass, typeParams, apiBuilder.baseApiType, typeUtils);

        SourceModule annotation = module.getAnnotation(SourceModule.class);
        if (annotation == null) {
            throw new IllegalStateException(
                    module.getQualifiedName() + " must have stash.annotations.SourceModule annotation");
        }

        ClassName sourceType;
        try {
            sourceType = ClassName.get(annotation.value());
        } catch (MirroredTypeException e) {
            TypeMirror mirror = e.getTypeMirror();
            TypeElement typeElement = (TypeElement) typeUtils.asElement(mirror);
            sourceType = ClassName.get(typeElement);
        }
        this.sourceType = sourceType;
        this.simple = annotation.simple();

        StashLog.d(TAG, "module %s", module.getQualifiedName());
        ExecutableElement constructor = null;
        ExecutableElement buildMethod = null;
        final List<String> orderedMethodNames = new ArrayList<>();
        final Map<String, List<ExecutableElement>> methods = new HashMap<>();
        for (Element sourceElement : module.getEnclosedElements()) {
            switch (sourceElement.getKind()) {
                case CONSTRUCTOR:
                    StashLog.d(TAG, "module constructor: %s", sourceElement.getSimpleName());
                    constructor = (ExecutableElement) sourceElement;
                    if (constructor.getParameters().size() != 0) {
                        throw new IllegalStateException("constructor must have no args");
                    }
                    break;
                case METHOD:
                    StashLog.d(TAG, "module method: %s", sourceElement.getSimpleName());
                    ExecutableElement method = (ExecutableElement) sourceElement;
                    String name = method.getSimpleName().toString();
                    if ("build".equals(name)) {
                        if (method.getParameters().size() != 0) {
                            throw new IllegalStateException("build() method must not have arguments");
                        }
                        buildMethod = method;
                        break;
                    } else if ("buildModules".equals(name)) {
                        if (method.getParameters().size() != 0) {
                            throw new IllegalStateException("buildModules() method must not have arguments");
                        }
                        buildMethod = method;
                        break;
                    } else if (method.getModifiers().contains(Modifier.PUBLIC)) {
                        List<ExecutableElement> list = methods.get(name);
                        if (list == null) {
                            list = new ArrayList<>();
                            methods.put(name, list);
                            orderedMethodNames.add(name);
                        }
                        list.add(method);
                    }
                    break;
                case FIELD:
                    StashLog.d(TAG, "module field: %s", sourceElement.getSimpleName());
                    break;
                default:
                    StashLog.d(TAG, "module element: %s", sourceElement.getSimpleName());
            }
        }
        if (constructor == null) {
            throw new IllegalStateException("constructor must not be null");
        } else if (buildMethod == null) {
            throw new IllegalStateException("must have a build() or buildModules() method");
        } else if (orderedMethodNames.size() == 0) {
            throw new IllegalStateException("no methods to process");
        }
        this.buildMethod = buildMethod;
        final Map<String, List<ExecutableElement>> finalMethods = new HashMap<>(methods.size());
        for (Map.Entry<String, List<ExecutableElement>> entry : methods.entrySet()) {
            finalMethods.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        this.methods = Collections.unmodifiableMap(finalMethods);
        this.orderedMethodNames = Collections.unmodifiableList(orderedMethodNames);
    }

    ClassName getSourceType() {
        return sourceType;
    }

    void writeTo(TypeSpec.Builder classBuilder) {
        ClassName builderName = apiBuilder.builderClassName;
        String packageName = builderName.packageName();
        String builderSimpleName = builderName.simpleName();
        TypeName builderType = apiBuilder.builderTypeName;

        String name = sourceType.simpleName() + "ModuleBuilder";
        ClassName moduleBuilderType = ClassName.get(packageName, builderSimpleName, name);
        TypeSpec.Builder moduleBuilder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        // Builder field
        FieldSpec builder = FieldSpec.builder(moduleType, "builder")
                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                .initializer("new $T()", moduleType)
                .build();
        moduleBuilder.addField(builder);

        // Constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        moduleBuilder.addMethod(constructor);

        boolean simple = this.simple && methods.size() == 1;
        if (!simple) {
            String methodName = "build" + sourceType.simpleName();
            MethodSpec spec = MethodSpec.methodBuilder(methodName)
                    .addAnnotation(NonNull)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(moduleBuilderType)
                    .addStatement("return new $T()", moduleBuilderType)
                    .build();
            classBuilder.addMethod(spec);
        }
        for (String methodName : orderedMethodNames) {
            for (ExecutableElement ee : nullSafe(methods.get(methodName))) {
                MethodSpec.Builder spec = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(NonNull)
                        .returns(moduleBuilderType);
                Util.copyTypeParams(ee, spec);
                String params = Util.copyParameters(ee, spec, apiBuilder.baseApiType, typeUtils);
                spec.addStatement("builder.$L($L)", methodName, params)
                        .addStatement("return this");
                moduleBuilder.addMethod(spec.build());

                if (simple) {
                    spec = MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(NonNull)
                            .returns(builderType);
                    Util.copyTypeParams(ee, spec);
                    params = Util.copyParameters(ee, spec, apiBuilder.baseApiType, typeUtils);
                    spec.addCode(CodeBlock.builder()
                            .add("return new $T()\n", moduleBuilderType)
                            .indent().indent().indent().indent()
                            .add(".$L($L)\n", methodName, params)
                            .addStatement(".add()")
                            .unindent().unindent().unindent().unindent()
                            .build());
                    classBuilder.addMethod(spec.build());
                }
            }
        }

        String buildMethodName = buildMethod.getSimpleName().toString();
        String addModule = buildMethodName.equals("build")
                ? "addModule"
                : "addModules";

        // Build method
        MethodSpec build = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NonNull)
                .returns(builderType)
                .addStatement("return $L(builder.$L())", addModule, buildMethodName)
                .build();
        moduleBuilder.addMethod(build);

        classBuilder.addType(moduleBuilder.build());
    }
}
