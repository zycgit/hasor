/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.boot.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

/**
 * Gradle integration for building Hasor Boot executable archives.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-07-14
 */
public class HasorBootGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        TaskProvider<HasorBootJar> bootJar = project.getTasks().register("bootJar", HasorBootJar.class, task -> {
            task.setGroup("build");
            task.setDescription("Assembles a Hasor Boot executable archive.");
            task.getArchiveClassifier().convention("boot");
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("libs"));
            task.getSourceJar().set(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class).flatMap(Jar::getArchiveFile));
            task.getRuntimeClasspath().from(project.getConfigurations().named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
        });

        project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure(task -> task.dependsOn(bootJar));
    }
}
