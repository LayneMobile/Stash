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

package stash.samples.hockeyloader.network.model;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.immutables.value.Value.Style.ImplementationVisibility.PACKAGE;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
        get = {"is*", "get*", "has*", "should*"}, // Detect 'get' and 'is' prefixes in accessor methods
        init = "set*", // Builder initialization methods will have 'set' prefix
        typeAbstract = {"*"}, // 'Abstract' prefix will be detected and trimmed
        typeImmutable = "*Impl", // No prefix or suffix for generated immutable type
        typeImmutableEnclosing = "*Impl", // No prefix or suffix for generated immutable enclosing type
        typeModifiable = "*Editor",
        instance = "instance",
        deepImmutablesDetection = true,
        builder = "new", // construct builder using 'new' instead of factory method
        builderVisibility = Value.Style.BuilderVisibility.SAME,
        overshadowImplementation = true,
        visibility = PACKAGE, // Generated class will be the same
        defaults = @Value.Immutable(copy = false)) // Disable copy methods by default
public @interface ModelStyle {}
