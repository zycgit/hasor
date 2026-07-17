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
package net.hasor.core;

import java.util.Collections;
import java.util.List;

/** Thrown when the container discovers that a bean depends on itself through a creation chain. */
public class CircularDependencyException extends IllegalStateException {
    /** Bind metadata key used to provide a more useful creation description. */
    public static final String DEPENDENCY_DESCRIPTION = "hasor.dependency.description";

    private final List<String> dependencyPath;

    public CircularDependencyException(List<String> dependencyPath) {
        super(buildMessage(dependencyPath));
        this.dependencyPath = List.copyOf(dependencyPath);
    }

    /** Returns the cycle path, including the repeated bean at the end. */
    public List<String> getDependencyPath() {
        return Collections.unmodifiableList(this.dependencyPath);
    }

    private static String buildMessage(List<String> path) {
        StringBuilder message = new StringBuilder("Circular bean dependency detected.\n\nDependency path:\n");
        for (int i = 0; i < path.size(); i++) {
            boolean last = i == path.size() - 1;
            message.append(last ? "\\-- " : "+-- ").append(path.get(i));
            if (last) {
                message.append("  <--- cycle closes here");
            } else {
                message.append("\n|   depends on\n");
            }
        }
        message.append("\n\nBreak the cycle by redesigning the dependency or injecting a lazy provider.");
        return message.toString();
    }
}
