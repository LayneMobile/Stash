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

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import okio.BufferedSink;
import okio.Okio;
import stash.model.ExtensionClass;
import stash.model.ExtensionClassKind;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class ExtensionProcessor extends AbstractProcessor {

    private final Map<ExtensionClassKind, Set<ExtensionClass>> classMap
            = new HashMap<>(ExtensionClassKind.values().length);
    private Filer filer;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;
        for (ExtensionClassKind kind : ExtensionClassKind.values()) {
            Set<ExtensionClass> classes = classMap.get(kind);
            for (Element element : env.getElementsAnnotatedWith(kind.annotationType)) {
                processed = true;

                // Ensure it is a class element
                if (element.getKind() != ElementKind.CLASS) {
                    error(element, "Only classes can be annotated with @%s", kind.annotationType.getSimpleName());
                    return true; // Exit processing
                }

                TypeElement typeElement = (TypeElement) element;
                ExtensionClass extensionClass = ExtensionClass.parse(typeElement);

                if (classes == null) {
                    classes = new HashSet<>();
                    classMap.put(kind, classes);
                }
                classes.add(extensionClass);
            }
        }

        if (processed) {
            for (Map.Entry<ExtensionClassKind, Set<ExtensionClass>> entry : classMap.entrySet()) {
                ExtensionClassKind kind = entry.getKey();
                List<ExtensionClass> classes = new ArrayList<>(entry.getValue());
                try {
                    FileObject output
                            = filer.createResource(StandardLocation.CLASS_OUTPUT, "", kind.resourceFilePath());
                    try (BufferedSink sink = Okio.buffer(Okio.sink(output.openOutputStream()))) {
                        // Write method data to buffer
                        kind.writeMethods(sink, classes);
                        sink.flush();
                    }
                } catch (IOException e) {
                    error("Error writing: %s", e.getMessage());
                    return true;
                }

                // TODO: find a way to filter out these classes from jar (make provided)
//                try {
//                    Buffer sink = new Buffer();
//                    kind.writeMethods(sink, classList);
//
//                    SourceWriter sourceWriter = kind.sourceWriter();
//                    sourceWriter.read(sink);
//
//                    sourceWriter.writeTo(filer);
//                } catch (IOException e) {
//                    // ignore
//                }
            }
        }

        return processed;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return ExtensionClassKind.supportedAnnotationTypes();
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message);
    }
}
