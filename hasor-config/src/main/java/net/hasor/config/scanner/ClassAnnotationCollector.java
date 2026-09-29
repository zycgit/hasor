/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.util.Set;
import net.hasor.cobble.asm.AnnotationVisitor;
import net.hasor.cobble.asm.ClassVisitor;
import net.hasor.cobble.asm.MethodVisitor;
import net.hasor.cobble.asm.Opcodes;

/** Collects registration annotations without loading unrelated application classes. */
final class ClassAnnotationCollector extends ClassVisitor {
    private final Set<String> supported;
    private final Set<String> matched;

    ClassAnnotationCollector(Set<String> supported, Set<String> matched) {
        super(Opcodes.ASM9);
        this.supported = supported;
        this.matched = matched;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        String name = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
        if (visible && this.supported.contains(name)) {
            this.matched.add(name);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodAnnotationCollector(this);
    }
}