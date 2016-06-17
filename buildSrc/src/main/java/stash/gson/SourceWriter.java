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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.lang.model.element.Modifier;

import okio.BufferedSource;
import okio.Okio;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static stash.Constants.GSON;
import static stash.Constants.GSON_BUILDER;
import static stash.Constants.GSON_FACTORY;
import static stash.Constants.OVERRIDE;
import static stash.Constants.TYPE_ADAPTER_FACTORY;
import static stash.Constants.TYPE_ADAPTER_FACTORY_ARRAY;

public final class SourceWriter {
    private static final String PACKAGE_NAME = "stash.util.gson";
    private static final String CLASS_NAME = "ImmutableGson";
    private static final String TYPE_ADAPTER_FACTORY_PATH = "META-INF/services/com.google.gson.TypeAdapterFactory";
    private static final String INDENT = "    ";

    private final ClassBuilder classBuilder;
    private final Set<String> adapters = new TreeSet<>();

    SourceWriter() {
        this.classBuilder = new ClassBuilder(PACKAGE_NAME, CLASS_NAME);
    }

    public void read(InputStream is) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (matches(entry)) {
                    String line;
                    BufferedSource source = Okio.buffer(Okio.source(zis));
                    while ((line = source.readUtf8Line()) != null) {
                        adapters.add(line);
                    }
                }
            }
        }
    }

    public void write(File outputDir) throws IOException {
        String packagePath = classBuilder.javaPackagePath();
        if (!packagePath.isEmpty()) {
            outputDir = new File(outputDir, packagePath);
        }
        outputDir.mkdirs();
        File outputFile = new File(outputDir, classBuilder.javaFileName());

        // Write data
        TypeSpec typeSpec = writeTypeSpec();

        // Write java file
        JavaFile javaFile = JavaFile.builder(classBuilder.packageName, typeSpec)
                .indent(INDENT)
                .build();

        try (Writer writer = new BufferedWriter(new FileWriter(outputFile))) {
            javaFile.writeTo(writer);
            writer.flush();
        }
    }

    public boolean matches(ZipEntry entry) {
        return TYPE_ADAPTER_FACTORY_PATH.equals(entry.getName());
    }

    private TypeSpec writeTypeSpec() {
        ClassName className = classBuilder.className();
        FieldSpec field_INSTANCE = FieldSpec.builder(className, "INSTANCE")
                .addModifiers(PRIVATE, STATIC, FINAL)
                .build();
        FieldSpec field_factories = FieldSpec.builder(TYPE_ADAPTER_FACTORY_ARRAY, "factories")
                .addModifiers(PRIVATE, FINAL)
                .build();
        FieldSpec field_gson = FieldSpec.builder(GSON, "gson")
                .addModifiers(PRIVATE, FINAL)
                .build();

        MethodSpec method_instance = MethodSpec.methodBuilder("instance")
                .addModifiers(PUBLIC, STATIC)
                .returns(className)
                .addStatement("return $N", field_INSTANCE)
                .build();

        MethodSpec method_newGsonBuilder_factories = MethodSpec.methodBuilder("newGsonBuilder")
                .addModifiers(PRIVATE, STATIC)
                .returns(GSON_BUILDER)
                .addParameter(TYPE_ADAPTER_FACTORY_ARRAY, "factories")
                .addStatement("$T gsonBuilder = new $T()", GSON_BUILDER, GSON_BUILDER)
                .beginControlFlow("for ($T factory : factories)", TYPE_ADAPTER_FACTORY)
                .addStatement("gsonBuilder.registerTypeAdapterFactory(factory)")
                .endControlFlow()
                .addStatement("return gsonBuilder")
                .build();

        CodeBlock staticBlock = CodeBlock.builder()
                .add(factories())
                .addStatement("$N = new $T(factories)", field_INSTANCE, className)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TYPE_ADAPTER_FACTORY_ARRAY, "factories")
                .addStatement("this.$N = factories", field_factories)
                .addCode(CodeBlock.builder()
                        .add("$[")
                        .add("this.$N = $N(factories)\n", field_gson, method_newGsonBuilder_factories)
                        .add(".create();\n")
                        .add("$]")
                        .build())
                .build();

        MethodSpec method_gson = MethodSpec.methodBuilder("gson")
                .returns(GSON)
                .addModifiers(PUBLIC)
                .addAnnotation(OVERRIDE)
                .addStatement("return $N", field_gson)
                .build();
        MethodSpec method_newGsonBuilder = MethodSpec.methodBuilder("newGsonBuilder")
                .addModifiers(PUBLIC)
                .addAnnotation(OVERRIDE)
                .returns(GSON_BUILDER)
                .addStatement("return $N($N)", method_newGsonBuilder_factories, field_factories)
                .build();

        return classBuilder.newSpecBuilder()
                .addModifiers(PUBLIC, FINAL)
                .addSuperinterface(GSON_FACTORY)
                .addField(field_INSTANCE)
                .addField(field_factories)
                .addField(field_gson)
                .addStaticBlock(staticBlock)
                .addMethod(constructor)
                .addMethod(method_instance)
                .addMethod(method_gson)
                .addMethod(method_newGsonBuilder)
                .addMethod(method_newGsonBuilder_factories)
                .build();
    }

    private CodeBlock factories() {
        CodeBlock.Builder factoryBuilder = CodeBlock.builder()
                .add("$[")
                .add("final $T factories = new $T{\n", TYPE_ADAPTER_FACTORY_ARRAY, TYPE_ADAPTER_FACTORY_ARRAY);
        boolean first = true;
        for (String adapter : adapters) {
            if (!first) {
                factoryBuilder.add(",\n");
            }
            first = false;
            ClassName className = ClassName.bestGuess(adapter);
            factoryBuilder.add("new $T()", className);
        }
        return factoryBuilder
                .add("\n$]")
                .add("};\n")
                .build();
    }
}
