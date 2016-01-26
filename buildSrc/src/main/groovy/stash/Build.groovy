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

package stash

import org.gradle.api.Project

public final class Build {
    public static boolean isPublishProject(Project project) {
        List<String> taskNames = project.gradle.startParameter.taskNames
        def upload1 = 'bintrayUpload' as String
        def upload2 = ":${project.name}:bintrayUpload" as String
        boolean isUpload = taskNames.contains(upload1) ||
                taskNames.contains(upload2)
        println "project: ${project.name} isPublishProject? ${isUpload}"
        return isUpload
    }

    public static boolean isPublish(Project project) {
        for (String taskName : project.gradle.startParameter.taskNames) {
            if (taskName.contains('bintrayUpload' as String)) {
                println "project: ${project.name} isPublish? true"
                return true
            }
        }
        println "project: ${project.name} isPublish? false"
        return false
    }
}
