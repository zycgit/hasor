/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.render;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Taking a response writer/stream transfers body ownership to application code, even before flush. */
public final class OwnedResponse extends HttpServletResponseWrapper {
    private boolean owned;

    public OwnedResponse(HttpServletResponse response) {
        super(response);
    }

    public boolean isOwned() {
        return this.owned;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        PrintWriter writer = super.getWriter();
        this.owned = true;
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        ServletOutputStream stream = super.getOutputStream();
        this.owned = true;
        return stream;
    }

    @Override
    public void reset() {
        super.reset();
        this.owned = false;
    }
}
