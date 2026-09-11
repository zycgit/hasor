/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.scanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import net.hasor.core.ApiBinder;

/** 处理带注解的类或方法，不自行扫描类路径。 */
public interface AnnotationProcessor<T extends AnnotatedElement> {
    List<Class<? extends Annotation>> annotationTypes();

    void process(ApiBinder binder, List<T> elements) throws Throwable;
}
