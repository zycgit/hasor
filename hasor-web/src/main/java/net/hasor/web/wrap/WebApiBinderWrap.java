/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.wrap;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import net.hasor.core.binder.ApiBinderWrap;
import net.hasor.web.*;

/**
 * {@link WebApiBinder} 接口包装器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class WebApiBinderWrap extends ApiBinderWrap implements WebApiBinder {
    private final WebApiBinder webApiBinder;

    public WebApiBinderWrap(WebApiBinder apiBinder) {
        super(apiBinder);
        this.webApiBinder = Objects.requireNonNull(apiBinder);
    }

    public ServletContext getServletContext() {
        return this.webApiBinder.getServletContext();
    }

    @Override
    public String getMimeType(String suffix) {
        return this.webApiBinder.getMimeType(suffix);
    }

    @Override
    public WebApiBinder setRequestCharacter(String encoding) {
        return this.webApiBinder.setRequestCharacter(encoding);
    }

    @Override
    public WebApiBinder setResponseCharacter(String encoding) {
        return this.webApiBinder.setResponseCharacter(encoding);
    }

    @Override
    public ServletVersion getServletVersion() {
        return webApiBinder.getServletVersion();
    }

    @Override
    public net.hasor.web.binder.ResourceBinder addResource(String pathPattern, net.hasor.cobble.loader.ResourceLoader... loaders) {
        return this.webApiBinder.addResource(pathPattern, loaders);
    }

    @Override
    public FilterBindingBuilder<InvokerFilter> filter(String[] morePatterns) {
        return this.webApiBinder.filter(morePatterns);
    }

    @Override
    public FilterBindingBuilder<InvokerFilter> filterRegex(String[] regexes) {
        return this.webApiBinder.filterRegex(regexes);
    }

    @Override
    public FilterBindingBuilder<Filter> jeeFilter(String[] morePatterns) {
        return this.webApiBinder.jeeFilter(morePatterns);
    }

    @Override
    public FilterBindingBuilder<Filter> jeeFilterRegex(String[] regexes) {
        return this.webApiBinder.jeeFilterRegex(regexes);
    }

    @Override
    public ServletBindingBuilder jeeServlet(String[] moreMappingTo) {
        return this.webApiBinder.jeeServlet(moreMappingTo);
    }

    @Override
    public List<Mapping> getMappings() {
        return this.webApiBinder.getMappings();
    }

    @Override
    public <T> MappingToBindingBuilder<T> mappingTo(String[] morePatterns) {
        return this.webApiBinder.mappingTo(morePatterns);
    }

    @Override
    public void addMimeType(String type, String mimeType) {
        this.webApiBinder.addMimeType(type, mimeType);
    }

    @Override
    public void loadMimeType(Reader reader) throws IOException {
        this.webApiBinder.loadMimeType(reader);
    }

    @Override
    public WebApiBinder bindInterceptor(HandlerInterceptor interceptor) {
        this.webApiBinder.bindInterceptor(interceptor);
        return this;
    }

    @Override
    public <E extends Throwable> WebApiBinder addExceptionHandler(Class<E> e, ExceptionHandler<? super E> handler) {
        this.webApiBinder.addExceptionHandler(e, handler);
        return this;
    }

    @Override
    public RenderEngineBindingBuilder addRender(String renderName) {
        return this.webApiBinder.addRender(renderName);
    }
}
