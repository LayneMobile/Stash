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

package stash.gson

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory

import java.util.zip.ZipFile

public class GenerateSourceTask extends DefaultTask {
    @InputFiles
    Iterable<File> inputFiles

    @OutputDirectory
    File outputDir

    public GenerateSourceTask() {
        group = "Artifact"
        description = "Generates GsonUtil sources"
    }

    public void init() {
        inputs.files(inputFiles)
        outputs.dir(outputDir)
        doLast {
            write()
        }
    }

    public void write() {
        SourceWriter writer = new SourceWriter();
        inputFiles.each { file ->
            if (!file.exists()) {
                return
            }
            if (file.name.contains(".aar")) {
                def zip = new ZipFile(file)
                try {
                    def zipEntry = zip.getEntry("classes.jar")
                    if (zipEntry == null) {
                        throw new IllegalStateException("can't find classes")
                    }
                    writer.read(zip.getInputStream(zipEntry))
                } finally {
                    zip.close()
                }
            } else if (file.name.contains(".jar")) {
                writer.read(new FileInputStream(file))
            }
        }
        writer.write(outputDir)
    }
}
