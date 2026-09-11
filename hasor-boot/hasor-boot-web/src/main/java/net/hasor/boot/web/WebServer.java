/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.web;
/**
 * Embedded HTTP server used to run Hasor Web MVC without a web.xml deployment.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-17
 */
public interface WebServer extends AutoCloseable {
    /** Start the HTTP server and initialize Hasor Web. */
    void start() throws Exception;

    /** Stop the HTTP server and destroy the servlet context. */
    void stop() throws Exception;

    /** Wait until the HTTP server is stopped. */
    void join() throws InterruptedException;

    /** Returns {@code true} after {@link #start()} succeeds and before {@link #stop()} completes. */
    boolean isStart();

    /** Host name or address configured for the server. */
    String getHost();

    /** Bound HTTP port. When port is configured as 0, this should be the actual local port after start. */
    int getPort();

    /** Servlet context path. */
    String getContextPath();

    @Override
    default void close() throws Exception {
        stop();
    }
}
