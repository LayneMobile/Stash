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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;

import stash.internal.StashLog;

import static stash.Constants.BaseApi;
import static stash.Constants.Builder;
import static stash.Constants.List;
import static stash.Constants.RequestProcessor;
import static stash.Constants.RequestProcessorBuilder;
import static stash.Constants.Source;
import static stash.Constants.SourceBuilder;

final class Util {
    private static final String TAG = Util.class.getSimpleName();

    static void copyTypeParams(ExecutableElement method, MethodSpec.Builder spec) {
        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            String typeParamName = typeParameterElement.getSimpleName().toString();
            List<TypeName> bounds = new ArrayList<>();
            for (TypeMirror bound : typeParameterElement.getBounds()) {
                bounds.add(TypeName.get(bound));
            }
            spec.addTypeVariable(
                    TypeVariableName.get(typeParamName, bounds.toArray(new TypeName[bounds.size()])));
        }
    }

    static String copyParameters(ExecutableElement method, MethodSpec.Builder spec,
            ContainerType containerType, Types typeUtils) {
        List<String> paramNames = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            String paramName = param.getSimpleName().toString();
            paramNames.add(paramName);
            TypeName paramType = TypeName.get(param.asType());
            if (paramType instanceof ParameterizedTypeName) {
                ParameterizedTypeName ptn = (ParameterizedTypeName) paramType;
                paramType = coallesceParamType(ptn.rawType, ptn.typeArguments, containerType, typeUtils);
            }

            Set<Modifier> modifiers = param.getModifiers();
            ParameterSpec.Builder paramSpec = ParameterSpec.builder(paramType, paramName)
                    .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]));
            for (AnnotationMirror am : method.getAnnotationMirrors()) {
                TypeElement te = (TypeElement) am.getAnnotationType().asElement();
                paramSpec.addAnnotation(ClassName.get(te));
            }
            spec.addParameter(paramSpec.build());
        }
        boolean first = true;
        StringBuilder paramString = new StringBuilder();
        for (String paramName : paramNames) {
            if (!first) {
                paramString.append(", ");
            }
            first = false;
            paramString.append(paramName);
        }
        return paramString.toString();
    }

    static ParameterizedTypeName builder(TypeName ofType) {
        return paramType(Builder, ofType);
    }

    static ParameterizedTypeName list(TypeName ofType) {
        return paramType(List, ofType);
    }

    static ParameterizedTypeName sourceBuilder(List<? extends TypeName> paramTypes) {
        return twoType(SourceBuilder, paramTypes);
    }

    static ParameterizedTypeName source(List<? extends TypeName> paramTypes) {
        return twoType(Source, paramTypes);
    }

    static ParameterizedTypeName requestProcessor(List<? extends TypeName> paramTypes) {
        return twoType(RequestProcessor, paramTypes);
    }

    static ParameterizedTypeName requestProcessorBuilder(List<? extends TypeName> paramTypes) {
        return twoType(RequestProcessorBuilder, paramTypes);
    }

    private static ParameterizedTypeName twoType(ClassName rawType,
            List<? extends TypeName> paramTypes) {
        if (paramTypes.size() != 2) {
            throw new IllegalArgumentException("must be 2 Type Parameters. Instead contains " + paramTypes.size());
        }
        return (ParameterizedTypeName) paramType(rawType, paramTypes);
    }

    static List<TypeVariableName> parseTypeParams(TypeElement typeElement) {
        List<TypeVariableName> typeParams = new ArrayList<>();
        for (TypeParameterElement typeParam : typeElement.getTypeParameters()) {
            typeParams.add((TypeVariableName) TypeName.get(typeParam.asType()));
        }
        return Collections.unmodifiableList(typeParams);
    }

    static TypeName paramType(ClassName rawType, List<? extends TypeName> paramTypes) {
        if (paramTypes.isEmpty()) {
            return rawType;
        }
        return paramType(rawType, paramTypes.toArray(new TypeName[paramTypes.size()]));
    }


    static TypeName coallesceParamType(ClassName rawType, List<? extends TypeName> paramTypes,
            ContainerType containerType, Types typeUtils) {
        List<? extends TypeMirror> containerTypes = new ArrayList<>(containerType.parameterizedType.typeArguments);
        List<TypeName> types = new ArrayList<>();

        NEXT:
        for (TypeName paramType : paramTypes) {
            StashLog.d(TAG, "paramTypeName: %s", paramType);
            if (!(paramType instanceof TypeVariableName)) {
                if (paramType instanceof ParameterizedTypeName) {
                    ParameterizedTypeName ptn = (ParameterizedTypeName) paramType;
                    paramType = coallesceParamType(ptn.rawType, ptn.typeArguments, containerType, typeUtils);
                } else {
                    StashLog.d(TAG, "Don't know how to handle typeName: %s", paramType);
                }
                types.add(paramType);
                continue;
            }
            StashLog.d(TAG, "paramTypeBounds: %s", ((TypeVariableName) paramType).bounds);
            TypeVariableName typeName = (TypeVariableName) paramType;
            List<TypeName> bounds = typeName.bounds;
            if (bounds.size() != 0) {
                Iterator<? extends TypeMirror> containerTypesIterator = containerTypes.iterator();
                while (containerTypesIterator.hasNext()) {
                    TypeMirror containerMirror = containerTypesIterator.next();
                    TypeName containerTypeName = TypeName.get(containerMirror);
                    TypeName found = findTypeName(containerMirror, bounds, typeUtils);
                    if (found != null) {
                        StashLog.d(TAG, "found super type: %s", found);
                        types.add(containerTypeName);
                        containerTypesIterator.remove();
                        continue NEXT;
                    }
                }
            }
            types.add(paramType);
        }
        StashLog.d(TAG, "finalTypes: %s", types);
        return paramType(rawType, types);
    }

    private static TypeName findTypeName(TypeMirror type, List<? extends TypeName> bounds, Types typeUtils) {
        if (type == null || type.getKind() == TypeKind.NONE) return null;
        TypeName typeName = TypeName.get(type);
        StashLog.d(TAG, "checking type: %s", typeName);
        if (contains(bounds, typeName)) {
            StashLog.d(TAG, "found type: %s", typeName);
            return typeName;
        }
        Element element = typeUtils.asElement(type);
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            TypeElement typeElement = (TypeElement) element;
            TypeName found = findTypeName(typeElement.getSuperclass(), bounds, typeUtils);
            if (found != null) {
                return found;
            }
            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                found = findTypeName(interfaceType, bounds, typeUtils);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean contains(List<? extends TypeName> bounds, TypeName type) {
        if (bounds.contains(type)) {
            return true;
        }
        if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName ptn = (ParameterizedTypeName) type;
            for (TypeName bound : bounds) {
                if (bound instanceof ParameterizedTypeName) {
                    if (ptn.rawType.equals(((ParameterizedTypeName) bound).rawType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static ParameterizedTypeName paramType(ClassName rawType, TypeName... paramTypes) {
        return ParameterizedTypeName.get(rawType, paramTypes);
    }

    static ContainerType parseBaseApiType(TypeElement api, Types typeUtils) {
        ContainerType baseApiType = null;
        TypeMirror superClass = api.getSuperclass();
        while (baseApiType == null && superClass.getKind() != TypeKind.NONE) {
            TypeName superClassType = TypeName.get(superClass);
            if (superClassType instanceof ParameterizedTypeName) {
                ParameterizedTypeName ptn = (ParameterizedTypeName) superClassType;
                if (ptn.rawType.equals(BaseApi)) {
                    baseApiType = new ContainerType(ptn, superClass);
                }
            }
            if (baseApiType == null) {
                TypeElement superElement = (TypeElement) typeUtils.asElement(superClass);
                superClass = superElement.getSuperclass();
            }
        }
        if (baseApiType == null) {
            throw new IllegalStateException("Api must extend from stash.BaseApi");
        }
        return baseApiType;
    }

    static final class ContainerType {
        final ParameterizedTypeName typeName;
        final TypeMirror type;
        final ParameterizedType parameterizedType;

        private ContainerType(ParameterizedTypeName typeName, TypeMirror type) {
            this.typeName = typeName;
            this.type = type;
            this.parameterizedType = type.accept(new SimpleTypeVisitor7<ParameterizedType, Void>() {
                @Override public ParameterizedType visitDeclared(DeclaredType t, Void p) {
                    return new ParameterizedType(t);
                }
            }, null);
        }
    }

    static final class ParameterizedType {
        final ClassName rawType;
        final List<TypeMirror> typeArguments;

        public ParameterizedType(DeclaredType t) {
            this.rawType = ClassName.get((TypeElement) t.asElement());
            this.typeArguments = Collections.unmodifiableList(t.getTypeArguments());
        }
    }

    private Util() { throw new AssertionError("no instances"); }
}
