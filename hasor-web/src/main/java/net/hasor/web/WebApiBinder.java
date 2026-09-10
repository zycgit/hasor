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
package net.hasor.web;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.core.TypeSupplier;
import net.hasor.web.annotation.MappingTo;
import net.hasor.web.binder.ResourceBinder;
import net.hasor.web.render.Render;
import net.hasor.web.render.RenderEngine;
/**
 * 提供了注册Servlet和Filter的方法。
 * @version : 2016-12-26
 * @author 赵永春 (zyc@hasor.net)
 */
public interface WebApiBinder extends ApiBinder, MimeType {
    /** Configure static resources independently of business filters and rendering. */
    ResourceBinder addResource(String pathPattern, ResourceLoader... loaders);

    /**获取ServletContext对象。*/
    ServletContext getServletContext();

    /** 设置请求编码 */
    WebApiBinder setRequestCharacter(String encoding);

    /** 设置响应编码 */
    WebApiBinder setResponseCharacter(String encoding);

    /** 设置全局请求响应编码，后设置的值覆盖先前配置。Action 可以通过注解或响应 API 覆盖本次响应编码。 */
    default WebApiBinder setEncodingCharacter(String requestEncoding, String responseEncoding) {
        return this.setRequestCharacter(requestEncoding).setResponseCharacter(responseEncoding);
    }

    /**获取容器支持的Servlet版本。*/
    ServletVersion getServletVersion();

    /**使用 MappingTo 表达式，创建一个{@link ServletBindingBuilder}。*/
    default ServletBindingBuilder jeeServlet(String urlPattern, String... morePatterns) {
        return this.jeeServlet(ArrayUtils.add(morePatterns, urlPattern));
    }

    /**使用 MappingTo 表达式，创建一个{@link ServletBindingBuilder}。*/
    ServletBindingBuilder jeeServlet(String[] morePatterns);

    /** Snapshot of mappings registered so far, available during module configuration without creating beans. */
    List<Mapping> getMappings();

    /**使用 MappingTo 表达式，创建一个{@link MappingToBindingBuilder}。*/
    default <T> MappingToBindingBuilder<T> mappingTo(String urlPattern, String... morePatterns) {
        return this.mappingTo(ArrayUtils.add(morePatterns, urlPattern));
    }

    /**使用 MappingTo 表达式，创建一个{@link MappingToBindingBuilder}。*/
    <T> MappingToBindingBuilder<T> mappingTo(String[] morePatterns);

    /** 加载带有 @MappingTo 注解的类。 */
    default WebApiBinder loadMappingTo(Set<Class<?>> udfTypeSet) {
        return this.loadMappingTo(udfTypeSet, Matchers.anyClass(), null);
    }

    /** 加载带有 @MappingTo 注解的类。 */
    default WebApiBinder loadMappingTo(Set<Class<?>> maybeUdfTypeSet, Predicate<Class<?>> matcher, TypeSupplier typeSupplier) {
        if (maybeUdfTypeSet != null && !maybeUdfTypeSet.isEmpty()) {
            maybeUdfTypeSet.stream()//
                    .filter(matcher)//
                    .filter(Matchers.annotatedWithClass(MappingTo.class))//
                    .forEach(aClass -> loadMappingTo(aClass, typeSupplier));
        }
        return this;
    }

    /** 加载带有 @MappingTo 注解的类。 */
    default WebApiBinder loadMappingTo(Class<?> mappingType) {
        return loadMappingTo(mappingType, null);
    }

    /** 加载带有 @MappingTo 注解的类。 */
    default WebApiBinder loadMappingTo(Class<?> mappingType, final TypeSupplier typeSupplier) {
        Objects.requireNonNull(mappingType, "class is null.");
        int modifier = mappingType.getModifiers();
        if (Modifier.isInterface(modifier) || Modifier.isAbstract(modifier) || mappingType.isArray() || mappingType.isEnum()) {
            throw new IllegalStateException(mappingType.getName() + " must be normal Bean");
        }

        MappingTo[] annotationsByType = mappingType.getAnnotationsByType(MappingTo.class);
        if (annotationsByType == null || annotationsByType.length == 0) {
            throw new IllegalStateException(mappingType.getName() + " must be configure @MappingTo");
        }
        //
        if (HttpServlet.class.isAssignableFrom(mappingType)) {
            final Class<? extends HttpServlet> httpServletType = (Class<HttpServlet>) mappingType;
            Arrays.stream(annotationsByType).peek(mappingTo -> {
            }).forEach(mappingTo -> {
                if (!isSingleton(mappingType)) {
                    throw new IllegalStateException("HttpServlet " + mappingType + " must be Singleton.");
                }
                if (typeSupplier != null) {
                    jeeServlet(mappingTo.value()).with(() -> typeSupplier.get(httpServletType));
                } else {
                    jeeServlet(mappingTo.value()).with(httpServletType);
                }
            });
        } else {
            final Class<Object> mappingObjType = (Class<Object>) mappingType;
            Arrays.stream(annotationsByType).peek(mappingTo -> {
            }).forEach(mappingTo -> {
                if (typeSupplier != null) {
                    mappingTo(mappingTo.value()).with(mappingObjType, () -> typeSupplier.get(mappingObjType));
                } else {
                    mappingTo(mappingTo.value()).with(mappingType);
                }
            });
        }
        return this;
    }

    /**使用传统表达式，创建一个{@link FilterBindingBuilder}。*/
    default FilterBindingBuilder<InvokerFilter> filter(String urlPattern, String... morePatterns) {
        return this.filter(ArrayUtils.add(morePatterns, urlPattern));
    }

    /**使用传统表达式，创建一个{@link FilterBindingBuilder}。*/
    FilterBindingBuilder<InvokerFilter> filter(String[] morePatterns);

    /**使用正则表达式，创建一个{@link FilterBindingBuilder}。*/
    default FilterBindingBuilder<InvokerFilter> filterRegex(String regex, String... regexes) {
        return this.filter(ArrayUtils.add(regexes, regex));
    }

    /**使用正则表达式，创建一个{@link FilterBindingBuilder}。*/
    FilterBindingBuilder<InvokerFilter> filterRegex(String[] regexes);

    /**使用传统表达式，创建一个{@link FilterBindingBuilder}。*/
    default FilterBindingBuilder<Filter> jeeFilter(String urlPattern, String... morePatterns) {
        return this.jeeFilter(ArrayUtils.add(morePatterns, urlPattern));
    }

    /**使用传统表达式，创建一个{@link FilterBindingBuilder}。*/
    FilterBindingBuilder<Filter> jeeFilter(String[] morePatterns);

    /**使用正则表达式，创建一个{@link FilterBindingBuilder}。*/
    default FilterBindingBuilder<Filter> jeeFilterRegex(String regex, String... regexes) {
        return this.jeeFilterRegex(ArrayUtils.add(regexes, regex));
    }

    /**使用正则表达式，创建一个{@link FilterBindingBuilder}。*/
    FilterBindingBuilder<Filter> jeeFilterRegex(String[] regexes);

    void addMimeType(String type, String mimeType);

    default void loadMimeType(String resource) throws IOException {
        loadMimeType(StandardCharsets.UTF_8, resource);
    }

    default void loadMimeType(InputStream inputStream) throws IOException {
        loadMimeType(StandardCharsets.UTF_8, inputStream);
    }

    default void loadMimeType(Charset charset, String resource) throws IOException {
        loadMimeType(charset, Objects.requireNonNull(ResourcesUtils.getResourceAsStream(resource), resource + " is not exist"));
    }

    default void loadMimeType(Charset charset, InputStream inputStream) throws IOException {
        loadMimeType(new InputStreamReader(inputStream, charset));
    }

    void loadMimeType(Reader reader) throws IOException;

    /** 负责配置Filter */
    static interface FilterBindingBuilder<T> {
        default void through(Class<? extends T> filterKey) {
            this.through(0, filterKey, null);
        }

        default void through(T filter) {
            this.through(0, filter, null);
        }

        default void through(Supplier<? extends T> filterProvider) {
            this.through(0, filterProvider, null);
        }

        default void through(BindInfo<? extends T> filterRegister) {
            this.through(0, filterRegister, null);
        }

        default void through(Class<? extends T> filterKey, Map<String, String> initParams) {
            this.through(0, filterKey, initParams);
        }

        default void through(T filter, Map<String, String> initParams) {
            this.through(0, filter, initParams);
        }

        default void through(Supplier<? extends T> filterProvider, Map<String, String> initParams) {
            this.through(0, filterProvider, initParams);
        }

        default void through(BindInfo<? extends T> filterRegister, Map<String, String> initParams) {
            this.through(0, filterRegister, initParams);
        }

        default void through(int index, Class<? extends T> filterKey) {
            this.through(index, filterKey, null);
        }

        default void through(int index, T filter) {
            this.through(index, filter, null);
        }

        default void through(int index, Supplier<? extends T> filterProvider) {
            this.through(index, filterProvider, null);
        }

        default void through(int index, BindInfo<? extends T> filterRegister) {
            this.through(index, filterRegister, null);
        }

        void through(int index, Class<? extends T> filterKey, Map<String, String> initParams);

        void through(int index, T filter, Map<String, String> initParams);

        void through(int index, Supplier<? extends T> filterProvider, Map<String, String> initParams);

        void through(int index, BindInfo<? extends T> filterRegister, Map<String, String> initParams);
    }

    /**负责配置Servlet。*/
    static interface ServletBindingBuilder {
        default void with(Class<? extends HttpServlet> targetKey) {
            with(0, targetKey, null);
        }

        default void with(HttpServlet target) {
            with(0, target, null);
        }

        default void with(Supplier<? extends HttpServlet> targetProvider) {
            with(0, targetProvider, null);
        }

        default void with(BindInfo<? extends HttpServlet> targetInfo) {
            with(0, targetInfo, null);
        }

        default void with(Class<? extends HttpServlet> servletKey, Map<String, String> initParams) {
            this.with(0, servletKey, initParams);
        }

        default void with(HttpServlet servlet, Map<String, String> initParams) {
            this.with(0, servlet, initParams);
        }

        default void with(Supplier<? extends HttpServlet> servletProvider, Map<String, String> initParams) {
            this.with(0, servletProvider, initParams);
        }

        default void with(BindInfo<? extends HttpServlet> servletRegister, Map<String, String> initParams) {
            this.with(0, servletRegister, initParams);
        }

        default void with(int index, Class<? extends HttpServlet> targetKey) {
            this.with(index, targetKey, null);
        }

        default void with(int index, HttpServlet target) {
            this.with(index, target, null);
        }

        default void with(int index, Supplier<? extends HttpServlet> targetProvider) {
            this.with(index, targetProvider, null);
        }

        default void with(int index, BindInfo<? extends HttpServlet> targetInfo) {
            this.with(index, targetInfo, null);
        }

        void with(int index, Class<? extends HttpServlet> servletKey, Map<String, String> initParams);

        void with(int index, HttpServlet servlet, Map<String, String> initParams);

        void with(int index, Supplier<? extends HttpServlet> servletProvider, Map<String, String> initParams);

        void with(int index, BindInfo<? extends HttpServlet> servletRegister, Map<String, String> initParams);
    }

    /**负责配置MappingTo。*/
    static interface MappingToBindingBuilder<T> {
        default void with(Class<? extends T> targetKey) {
            with(0, targetKey);
        }

        default void with(T target) {
            with(0, target);
        }

        default void with(Class<T> referKey, Supplier<? extends T> targetProvider) {
            with(0, referKey, targetProvider);
        }

        default void with(BindInfo<? extends T> targetInfo) {
            with(0, targetInfo);
        }

        void with(int index, Class<? extends T> targetKey);

        void with(int index, T target);

        void with(int index, Class<T> referKey, Supplier<? extends T> targetProvider);

        void with(int index, BindInfo<? extends T> targetInfo);
    }
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** 加载带有 @Render注解配置的渲染器。 */
    default WebApiBinder loadRender(Set<Class<?>> udfTypeSet) {
        return this.loadRender(udfTypeSet, Matchers.anyClass(), null);
    }

    /** 加载带有 @Render注解配置的渲染器。 */
    default WebApiBinder loadRender(Set<Class<?>> maybeUdfTypeSet, Predicate<Class<?>> matcher, TypeSupplier typeSupplier) {
        if (maybeUdfTypeSet != null && !maybeUdfTypeSet.isEmpty()) {
            maybeUdfTypeSet.stream()//
                    .filter(matcher)//
                    .filter(Matchers.annotatedWithClass(Render.class))//
                    .forEach(aClass -> loadRender(aClass, typeSupplier));
        }
        return this;
    }

    /** 加载 @Render注解配置的渲染器。*/
    default WebApiBinder loadRender(Class<?> renderClass) {
        return loadRender(renderClass, null);
    }

    /** 加载 @Render注解配置的渲染器。*/
    default WebApiBinder loadRender(Class<?> renderClass, TypeSupplier typeSupplier) {
        Objects.requireNonNull(renderClass, "class is null.");
        int modifier = renderClass.getModifiers();
        if (Modifier.isInterface(modifier) || Modifier.isAbstract(modifier) || renderClass.isArray() || renderClass.isEnum()) {
            throw new IllegalStateException(renderClass.getName() + " must be normal Bean");
        }
        if (!renderClass.isAnnotationPresent(Render.class)) {
            throw new IllegalStateException(renderClass.getName() + " must be configure @Render");
        }
        if (!RenderEngine.class.isAssignableFrom(renderClass)) {
            throw new IllegalStateException(renderClass.getName() + " must be implements RenderEngine.");
        }
        //
        Class<RenderEngine> engineClass = (Class<RenderEngine>) renderClass;
        Render renderInfo = renderClass.getAnnotation(Render.class);
        if (renderInfo != null && renderInfo.value().length > 0) {
            for (String renderName : renderInfo.value()) {
                if (typeSupplier == null) {
                    addRender(renderName).to(engineClass);
                } else {
                    addRender(renderName).toProvider(() -> {
                        return typeSupplier.get(engineClass);
                    });
                }
            }
        }
        return this;
    }

    /**
     * 添加一个渲染器，用来将 Action 请求的结果渲染成页面。
     * @param renderName 渲染器名称
     */
    RenderEngineBindingBuilder addRender(String renderName);

    /** 负责配置RenderEngine。*/
    interface RenderEngineBindingBuilder {
        /**绑定实现。*/
        <T extends RenderEngine> void to(Class<T> renderEngineType);

        /**绑定实现。*/
        default void toInstance(RenderEngine renderEngine) {
            this.toProvider(() -> renderEngine);
        }

        /**绑定实现。*/
        void toProvider(Supplier<? extends RenderEngine> renderEngineProvider);

        /**绑定实现。*/
        void bindToInfo(BindInfo<? extends RenderEngine> renderEngineInfo);
    }
    //
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //    /** 创建一个映射，当匹配某个URL的时候使用指定的 Render 来渲染。如果存在多条规则按照 index 顺序裁决 index 最大的那一个*/
    //default void urlExtensionToRender(String extension, String renderName) {
    //        this.urlExtensionToRender(0, extension, renderName);
    //    }
    //
    //void urlExtensionToRender(int index, String extension, String renderName);
}
