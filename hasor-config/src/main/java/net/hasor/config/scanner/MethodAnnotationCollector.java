/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import net.hasor.cobble.asm.AnnotationVisitor;
import net.hasor.cobble.asm.MethodVisitor;
import net.hasor.cobble.asm.Opcodes;

/** Adds method annotations to the owning class's registration candidates. */
final class MethodAnnotationCollector extends MethodVisitor {
    private final ClassAnnotationCollector owner;

    MethodAnnotationCollector(ClassAnnotationCollector owner) {
        super(Opcodes.ASM9);
        this.owner = owner;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return this.owner.visitAnnotation(descriptor, visible);
    }
}