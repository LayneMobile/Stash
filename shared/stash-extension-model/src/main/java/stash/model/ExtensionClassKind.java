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
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;

import static stash.model.Constants.FILE_HEADER;
import static stash.model.Constants.FILE_VERSION;

public enum ExtensionClassKind {
    Module(stash.annotations.Module.class, "stash", "StashModule"),
    Stashes(stash.annotations.Stashes.class, "stash", "Stashes");

    private static final String OUTPUT_DIR = "META-INF/com.laynemobile.stash";
    private static final String FILE_EXTENSION = ".stash";

    public final Class<? extends Annotation> annotationType;
    public final String packageName;
    public final String className;
    public final String qualifiedName;

    ExtensionClassKind(Class<? extends Annotation> annotationType, String packageName, String className) {
        this.annotationType = annotationType;
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = packageName + "." + className;
    }

    public static Set<String> supportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        for (ExtensionClassKind kind : values()) {
            types.add(kind.annotationType.getCanonicalName());
        }
        return types;
    }

    public static Set<SourceWriter> sourceWriters() {
        Set<SourceWriter> writers = new HashSet<>();
        for (ExtensionClassKind kind : values()) {
            writers.add(new SourceWriter(kind));
        }
        return writers;
    }

    public void writeMethods(BufferedSink sink, List<ExtensionClass> classes) throws IOException {
        sink.write(FILE_HEADER);
        sink.writeInt(FILE_VERSION);

        int size = classes.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            ExtensionClass extensionClass = classes.get(i);
            if (this == Stashes) {
                extensionClass.writeMethods(sink, Modifier.STATIC);
            } else {
                extensionClass.writeMethods(sink);
            }
        }
    }

    public void readMethods(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
        ByteString header = source.readByteString(FILE_HEADER.size());
        if (!FILE_HEADER.equals(header)) {
            throw new IOException("Cannot read from the source. Header is not set");
        } else if (source.readInt() != FILE_VERSION) {
            throw new IOException("Cannot read from the source. Version is not current");
        }
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            ExtensionClass.readMethods(source, this, classBuilder);
        }
    }

    public ClassName className() {
        return ClassName.get(packageName, className);
    }

    public String resourceFileName() {
        return className + FILE_EXTENSION;
    }

    public String resourceFilePath() {
        return OUTPUT_DIR + "/" + resourceFileName();
    }

    public String javaPackagePath() {
        return packageName.replace('.', '/');
    }

    public String javaFileName() {
        return className + ".java";
    }
}
