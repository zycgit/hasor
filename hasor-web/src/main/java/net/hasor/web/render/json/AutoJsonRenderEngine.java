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
package net.hasor.web.render.json;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

/** Selects an optional JSON implementation in the same order as dbVisitor's JsonTypeHandler. */
public final class AutoJsonRenderEngine implements RenderEngine {
    private static final Logger     logger    = LoggerFactory.getLogger(AutoJsonRenderEngine.class);
    private static final String[][] PROVIDERS = {                       //
            { "com.fasterxml.jackson.databind.ObjectMapper", "JacksonRenderEngine" },//
            { "com.google.gson.Gson", "GsonRenderEngine" },             //
            { "com.alibaba.fastjson.JSON", "JsonRenderEngine" },        //
            { "com.alibaba.fastjson2.JSON", "Fastjson2RenderEngine" }   //
    };
    private final ClassLoader       classLoader;
    private volatile RenderEngine   delegate;

    public AutoJsonRenderEngine() {
        this(AutoJsonRenderEngine.class.getClassLoader());
    }

    public AutoJsonRenderEngine(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /** Resolve once, when actually selected, so explicit custom engines need no third-party JSON library. */
    public synchronized void initialize() {
        if (this.delegate != null) {
            return;
        }

        for (String[] provider : PROVIDERS) {
            try {
                Class.forName(provider[0], false, this.classLoader);
            } catch (ClassNotFoundException absent) {
                continue;
            } catch (LinkageError broken) {
                throw new IllegalStateException("JSON provider is present but cannot be linked: " + provider[0], broken);
            }

            try {
                Class<?> type = Class.forName(AutoJsonRenderEngine.class.getPackageName() + "." + provider[1], true, this.classLoader);
                this.delegate = (RenderEngine) type.getConstructor().newInstance();
                logger.info("JSON render provider: " + provider[0]);
                return;
            } catch (ReflectiveOperationException | LinkageError broken) {
                Throwable cause = broken instanceof InvocationTargetException invocation ? invocation.getCause() : broken;
                throw new IllegalStateException("Cannot initialize JSON provider: " + provider[0], cause);
            }
        }

        throw new IllegalStateException("No JSON provider found. Add Jackson 2 (jackson-databind), Gson, Fastjson or Fastjson2 " + "to the application runtime classpath, or register a custom 'json' RenderEngine.");
    }

    @Override
    public void process(RenderInvoker invoker, Writer writer) throws Throwable {
        if (this.delegate == null) {
            this.initialize();
        }

        this.delegate.process(invoker, writer);
    }
}