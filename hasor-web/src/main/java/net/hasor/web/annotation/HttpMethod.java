/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.annotation;
import java.lang.annotation.*;

/**
 * Associates the name of a HTTP method with an annotation. A Java method annotated
 * with a runtime annotation that is itself annotated with this annotation will
 * be used to handle HTTP requests of the indicated HTTP method. It is an error
 * for a method to be annotated with more than one annotation that is annotated
 * with {@code HttpMethod}.
 * @see HttpMethod#ANY
 * @see HttpMethod#GET
 * @see HttpMethod#POST
 * @see HttpMethod#PUT
 * @see HttpMethod#DELETE
 * @see HttpMethod#HEAD
 * @see HttpMethod#OPTIONS
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMethod {
    /** HTTP ANY method */
    String ANY     = "ANY";
    /** HTTP GET method */
    String GET     = "GET";
    /** HTTP POST method */
    String POST    = "POST";
    /** HTTP PUT method */
    String PUT     = "PUT";
    /** HTTP DELETE method */
    String DELETE  = "DELETE";
    /** HTTP HEAD method */
    String HEAD    = "HEAD";
    /** HTTP OPTIONS method */
    String OPTIONS = "OPTIONS";

    /** Specifies the name of a HTTP method. E.g. "GET". */
    String[] value();
}
