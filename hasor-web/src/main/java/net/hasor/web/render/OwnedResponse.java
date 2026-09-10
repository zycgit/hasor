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