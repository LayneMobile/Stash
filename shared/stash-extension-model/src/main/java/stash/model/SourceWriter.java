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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import okio.BufferedSource;
import okio.Okio;

import static stash.model.Constants.NON_NULL;

public final class SourceWriter {
    private final ExtensionClassKind kind;
    private final TypeSpec.Builder classBuilder;

    SourceWriter(ExtensionClassKind kind) {
        this.kind = kind;
        this.classBuilder = TypeSpec.classBuilder(kind.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // Constructor
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        if (kind == ExtensionClassKind.Stashes) {
            ClassName exception = ClassName.get(AssertionError.class);
            constructor.addStatement("throw new $T($S)", exception, "no instances");
        }
        classBuilder.addMethod(constructor.build());

        if (kind != ExtensionClassKind.Stashes) {
            // Instance field
            ClassName instanceType = kind.className();
            FieldSpec instanceField
                    = FieldSpec.builder(instanceType, "INSTANCE", Modifier.PRIVATE, Modifier.STATIC,
                    Modifier.FINAL)
                    .initializer("new $T()", instanceType)
                    .build();
            classBuilder.addField(instanceField);

            // Instance Method
            MethodSpec instanceMethod = MethodSpec.methodBuilder("getInstance")
                    .addAnnotation(NON_NULL)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("return INSTANCE")
                    .returns(instanceType)
                    .build();
            classBuilder.addMethod(instanceMethod);
        }
    }

    public void read(InputStream is) throws IOException {
        // Read method data from buffer into java source
        BufferedSource source = Okio.buffer(Okio.source(is));
        read(source);
    }

    public void read(BufferedSource source) throws IOException {
        kind.readMethods(source, classBuilder);
    }

    public void writeTo(Filer filer) throws IOException {
        JavaFileObject fileObject = filer.createSourceFile(kind.qualifiedName);
        writeTo(fileObject.openOutputStream());
    }

    public void write(File outputDir) throws IOException {
        String packagePath = kind.javaPackagePath();
        if (!packagePath.isEmpty()) {
            outputDir = new File(outputDir, packagePath);
        }
        outputDir.mkdirs();
        File outputFile = new File(outputDir, kind.javaFileName());
        writeTo(new FileOutputStream(outputFile));
    }

    private void writeTo(OutputStream out) throws IOException {
        // Write java file
        JavaFile javaFile = JavaFile.builder(kind.packageName, classBuilder.build())
                .build();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(out))) {
            javaFile.writeTo(writer);
            writer.flush();
        }
    }

    public boolean matches(ZipEntry entry) {
        return kind.resourceFilePath().equals(entry.getName());
    }
}
