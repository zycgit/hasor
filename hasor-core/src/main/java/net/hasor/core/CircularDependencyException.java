/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
