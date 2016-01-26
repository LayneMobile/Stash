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
import javax.lang.model.util.Types;

import stash.annotations.GenerateApiBuilder;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class Compiler extends AbstractProcessor {

    private Types typeUtils;
    private Filer filer;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        typeUtils = env.getTypeUtils();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;
        for (Kind kind : Kind.values()) {
            for (Element element : env.getElementsAnnotatedWith(kind.annotationType)) {
                processed = true;

                // Ensure it is a class element
                if (element.getKind() != ElementKind.CLASS) {
                    error(element, "Only classes can be annotated with @%s", kind.annotationType.getSimpleName());
                    return true; // Exit processing
                }

                TypeElement typeElement = (TypeElement) element;
                if (kind.annotationType == GenerateApiBuilder.class) {
                    try {
                        new ApiBuilder(typeElement, typeUtils)
                                .writeTo(filer);
                    } catch (IOException e) {
                        error("exception %s", e);
                        return true;
                    }
                }
            }
        }
        return processed;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return Kind.supportedAnnotationTypes();
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
