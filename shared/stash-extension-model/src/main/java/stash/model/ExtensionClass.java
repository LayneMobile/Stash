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


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import okio.BufferedSink;
import okio.BufferedSource;

import static stash.model.Constants.NON_NULL;
import static stash.model.Util.readAnnotations;
import static stash.model.Util.readClassName;
import static stash.model.Util.readModifiers;
import static stash.model.Util.readParams;
import static stash.model.Util.readTypeName;
import static stash.model.Util.readTypeParams;
import static stash.model.Util.readUtf8Entry;
import static stash.model.Util.writeAnnotations;
import static stash.model.Util.writeClassName;
import static stash.model.Util.writeModifiers;
import static stash.model.Util.writeParams;
import static stash.model.Util.writeTypeName;
import static stash.model.Util.writeTypeParams;
import static stash.model.Util.writeUtf8Entry;

public final class ExtensionClass {
    private final TypeElement element;
    private final ExtensionMethod instanceMethod;
    private final List<ExtensionMethod> methods;

    private ExtensionClass(TypeElement element, ExtensionMethod instanceMethod, List<ExtensionMethod> methods) {
        if (instanceMethod == null) {
            throw new IllegalArgumentException(element.getQualifiedName() + " must have an instance method specified");
        } else if (!ClassName.get(element).equals(TypeName.get(instanceMethod.method.getReturnType()))) {
            throw new IllegalArgumentException(
                    element.getQualifiedName() + " instance method must return its own type");
        } else if (methods.size() == 0) {
            throw new IllegalArgumentException(element.getQualifiedName() + " has no annotated methods to process");
        }
        this.element = element;
        this.instanceMethod = instanceMethod;
        this.methods = Collections.unmodifiableList(methods);
    }

    public static ExtensionClass parse(TypeElement element) {
        ExtensionMethod instanceMethod = null;
        List<ExtensionMethod> methods = new ArrayList<>();
        for (Element memberElement : element.getEnclosedElements()) {
            ExtensionMethod method = parseMethod(memberElement);
            if (method == null) continue;
            switch (method.kind) {
                case Instance:
                    if (instanceMethod != null) {
                        String format = "Cannot have instance method '%s' when '%s' is already defined";
                        String message = String.format(format, method.name(), instanceMethod.name());
                        throw new IllegalStateException(message);
                    }
                    instanceMethod = method;
                    break;
                default:
                    methods.add(method);
                    break;
            }
        }
        return new ExtensionClass(element, instanceMethod, methods);
    }

    private static ExtensionMethod parseMethod(Element memberElement) {
        ExtensionMethodKind methodKind = null;
        List<TypeElement> others = new ArrayList<>();
        for (AnnotationMirror am : memberElement.getAnnotationMirrors()) {
            ExtensionMethodKind kind = parseAnnotation(am, others);
            if (methodKind != null && kind != null) {
                String format = "Cannot have annotation '%s' when '%s' is already present";
                String message = String.format(format, kind.annotationType, methodKind.annotationType);
                throw new IllegalStateException(message);
            } else if (kind != null) {
                kind.validate(memberElement);
                methodKind = kind;
            }
        }
        return (methodKind == null) ? null
                : new ExtensionMethod(methodKind, (ExecutableElement) memberElement, others);
    }

    private static ExtensionMethodKind parseAnnotation(AnnotationMirror am, List<TypeElement> others) {
        TypeElement te = (TypeElement) am.getAnnotationType().asElement();
        String name = te.getQualifiedName().toString();
        for (ExtensionMethodKind annotationKind : ExtensionMethodKind.values()) {
            if (name.equals(annotationKind.annotationType.getCanonicalName())) {
                return annotationKind;
            }
        }
        others.add(te);
        return null;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtensionClass that = (ExtensionClass) o;

        return element.getQualifiedName().toString().equals(that.element.getQualifiedName().toString());
    }

    @Override public int hashCode() {
        return element.getQualifiedName().toString().hashCode();
    }

    void writeMethods(BufferedSink sink, Modifier... modifiers) throws IOException {
        int size = methods.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            ExtensionMethod extensionMethod = methods.get(i);
            ExecutableElement methodElement = extensionMethod.method;

            // Write method name
            String methodName = methodElement.getSimpleName().toString();
            writeUtf8Entry(sink, methodName);

            // Write modifiers
            Set<Modifier> mods = new HashSet<>(methodElement.getModifiers());
            mods.addAll(Arrays.asList(modifiers));
            writeModifiers(sink, mods);

            // Write type parameters
            writeTypeParams(sink, methodElement.getTypeParameters());

            // Write parameters
            String params = writeParams(sink, methodElement.getParameters());

            // Write return annotations
            writeAnnotations(sink, extensionMethod.kind, new ArrayList<>(extensionMethod.returnAnnotations));

            // Write classType
            writeClassName(sink, ClassName.get(element));

            // Write statement
            String statement = String.format("$T.%s().%s(%s)", instanceMethod.name(), extensionMethod.name(), params);
            writeUtf8Entry(sink, statement);

            // Write method kind
            ExtensionMethodKind methodKind = extensionMethod.kind;
            methodKind.write(sink);
            switch (methodKind) {
                case Return:
                    TypeName returnType = TypeName.get(methodElement.getReturnType());
                    writeTypeName(sink, returnType);
                    break;
                case ReturnThis:
                case Void:
                    break;
                default:
                    throw new IllegalStateException("invalid method kind");
            }
        }
    }

    static void readMethods(BufferedSource source, ExtensionClassKind kind, TypeSpec.Builder classBuilder)
            throws IOException {
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            // Read method name
            String methodName = readUtf8Entry(source);

            // Read modifiers
            Set<Modifier> modifiers = readModifiers(source);

            // Create method builder with modifiers
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]));

            // Read type parameters
            readTypeParams(source, methodBuilder);

            // Read parameters
            readParams(source, methodBuilder);

            // Read return annotations
            readAnnotations(source, methodBuilder);

            // Read classType
            ClassName classType = readClassName(source);

            // Read statement
            String statement = readUtf8Entry(source);

            // Read method kind
            ExtensionMethodKind methodKind = ExtensionMethodKind.read(source);
            switch (methodKind) {
                case Return:
                    TypeName returnType = readTypeName(source);
                    methodBuilder.returns(returnType);
                    statement = "return " + statement;
                    methodBuilder.addStatement(statement, classType);
                    break;
                case ReturnThis:
                    methodBuilder.returns(kind.className());
                    methodBuilder.addStatement(statement, classType);
                    methodBuilder.addStatement("return this");
                    methodBuilder.addAnnotation(NON_NULL);
                    break;
                case Void:
                    methodBuilder.addStatement(statement, classType);
                    break;
                default:
                    throw new IllegalStateException("invalid method kind");
            }

            // Add method to classbuilder
            classBuilder.addMethod(methodBuilder.build());
        }
    }
}
