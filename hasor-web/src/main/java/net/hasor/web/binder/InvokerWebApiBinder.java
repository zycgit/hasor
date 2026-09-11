/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.binder;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Supplier;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.dynamic.Matchers;
import net.hasor.cobble.provider.InstanceProvider;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.binder.ApiBinderWrap;
import net.hasor.web.InvokerFilter;
import net.hasor.web.Mapping;
import net.hasor.web.ServletVersion;
import net.hasor.web.WebApiBinder;
import net.hasor.web.mime.MimeTypeSupplier;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderProcessor;
import net.hasor.web.startup.RuntimeFilter;

/**
 * 该类是{@link WebApiBinder}接口实现。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-10
 */
public class InvokerWebApiBinder extends ApiBinderWrap implements WebApiBinder {
    private final InstanceProvider<String>        requestEncoding  = new InstanceProvider<>("");
    private final InstanceProvider<String>        responseEncoding = new InstanceProvider<>("");
    private final ServletVersion                  curVersion;
    private final MimeTypeSupplier                mimeType;
    private final List<Mapping>                   mappings         = new ArrayList<>();
    private final InstanceProvider<ResourceDef[]> resources        = new InstanceProvider<>(new ResourceDef[0]);
    private final List<InnerResourceBinder>       resourceBindings = new ArrayList<>();
    private final RenderProcessor                 renderProcessor  = new RenderProcessor();

    // ------------------------------------------------------------------------------------------------------

    protected InvokerWebApiBinder(ServletVersion curVersion, MimeTypeSupplier mimeType, ApiBinder apiBinder) {
        super(apiBinder);
        apiBinder.bindType(String.class).nameWith(RuntimeFilter.HTTP_REQUEST_ENCODING_KEY).toProvider(this.requestEncoding);
        apiBinder.bindType(String.class).nameWith(RuntimeFilter.HTTP_RESPONSE_ENCODING_KEY).toProvider(this.responseEncoding);
        apiBinder.bindType(ResourceDef[].class).toProvider(this.resources);
        apiBinder.bindType(RenderProcessor.class).toInstance(this.renderProcessor);
        this.curVersion = Objects.requireNonNull(curVersion);
        this.mimeType = Objects.requireNonNull(mimeType);
        this.registerConfiguredRenderEngines();
    }

    private void registerConfiguredRenderEngines() {
        SettingNode[] nodes = getSettings().getNodeArray("hasor.render.engines.engine");
        if (nodes == null) {
            return;
        }

        Map<String, String> definitions = new LinkedHashMap<>();
        for (SettingNode node : nodes) {
            String name = node.getSubValue("name");
            if (name == null || !name.matches("[A-Za-z][A-Za-z0-9_-]*")) {
                throw new IllegalArgumentException("Invalid render engine name: " + name);
            }
            definitions.put(name.toLowerCase(Locale.ROOT), node.getValue());
        }

        ClassLoader loader = getClassLoader();
        definitions.forEach((name, className) -> {
            // Keep class loading lazy: an explicit registration must be able to replace an unavailable implementation.
            BindInfo<? extends RenderEngine> info = bindType(RenderEngine.class).uniqueName().toProvider(() -> {
                return createConfiguredRenderEngine(name, className, loader);
            }).toInfo();
            bindType(RenderDef.class).uniqueName().toInstance(new RenderDef(name, info, true));
        });
    }

    private static RenderEngine createConfiguredRenderEngine(String name, String className, ClassLoader loader) {
        try {
            Class<? extends RenderEngine> type = Class.forName(className, true, loader).asSubclass(RenderEngine.class);
            try {
                return type.getConstructor(ClassLoader.class).newInstance(loader);
            } catch (NoSuchMethodException ignored) {
                return type.getConstructor().newInstance();
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            throw new IllegalStateException("Cannot create render engine '" + name + "': " + className, e);
        }
    }

    private static List<String> checkEmpty(List<String> patternArrays, String npeMessage) {
        boolean needThrow = true;
        for (String pattern : patternArrays) {
            if (StringUtils.isBlank(pattern)) {
                continue;
            }
            needThrow = false;
            break;
        }
        if (needThrow) {
            throw new NullPointerException(npeMessage);
        }
        return patternArrays;
    }

    @Override
    public ResourceBinder addResource(String pathPattern, net.hasor.cobble.loader.ResourceLoader... loaders) {
        InnerResourceBinder binding = new InnerResourceBinder(pathPattern, loaders);
        this.resourceBindings.add(binding);
        return binding;
    }

    void initialize(AppContext context) throws Throwable {
        this.initializeResources();
        this.initializeRendering(context);
    }

    private void initializeResources() {
        Comparator<InnerResourceBinder> specificity = Comparator.comparingInt(InnerResourceBinder::specificity).reversed();
        Comparator<InnerResourceBinder> priority = Comparator.comparingInt(InnerResourceBinder::getOrder).thenComparing(specificity);
        this.resources.set(this.resourceBindings.stream()//
                .sorted(priority)//
                .map(InnerResourceBinder::build)//
                .toArray(ResourceDef[]::new));
    }

    private void initializeRendering(AppContext context) throws Throwable {
        String objectEngine = getSettings().getString("hasor.render.defaults.objectEngine");
        String stringEngine = getSettings().getString("hasor.render.defaults.stringEngine");
        this.renderProcessor.doInit(context, objectEngine, stringEngine);
    }

    // ------------------------------------------------------------------------------------------------------

    @Override
    public ServletContext getServletContext() {
        return (ServletContext) this.getContext();
    }

    @Override
    public WebApiBinder setRequestCharacter(String encoding) {
        this.requestEncoding.set(encoding);
        return this;
    }

    @Override
    public WebApiBinder setResponseCharacter(String encoding) {
        this.responseEncoding.set(encoding);
        return this;
    }

    @Override
    public String getMimeType(String suffix) {
        return this.mimeType.getMimeType(suffix);
    }

    @Override
    public void addMimeType(String type, String mimeType) {
        this.mimeType.addMimeType(type, mimeType);
    }

    @Override
    public void loadMimeType(Reader reader) throws IOException {
        this.mimeType.loadReader(reader);
    }

    @Override
    public ServletVersion getServletVersion() {
        return this.curVersion;
    }

    // ------------------------------------------------------------------------------------------------------
    @Override
    public FilterBindingBuilder<InvokerFilter> filter(String[] morePatterns) {
        List<String> uriPatterns = checkEmpty(Arrays.asList(morePatterns), "Filter patterns is empty.");
        return new FiltersModuleBinder<InvokerFilter>(InvokerFilter.class, UriPatternType.SERVLET, uriPatterns) {
            @Override
            protected void bindThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends InvokerFilter> filterRegister, Map<String, String> initParams) {
                filterThrough(index, pattern, matcher, filterRegister, initParams);
            }
        };
    }

    @Override
    public FilterBindingBuilder<InvokerFilter> filterRegex(String[] regexes) {
        List<String> uriPatterns = checkEmpty(Arrays.asList(regexes), "Filter patterns is empty.");
        return new FiltersModuleBinder<>(InvokerFilter.class, UriPatternType.REGEX, uriPatterns) {
            @Override
            protected void bindThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends InvokerFilter> filterRegister, Map<String, String> initParams) {
                filterThrough(index, pattern, matcher, filterRegister, initParams);
            }
        };
    }

    @Override
    public FilterBindingBuilder<Filter> jeeFilter(final String[] morePatterns) throws NullPointerException {
        List<String> uriPatterns = checkEmpty(Arrays.asList(morePatterns), "Filter patterns is empty.");
        return new FiltersModuleBinder<>(Filter.class, UriPatternType.SERVLET, uriPatterns) {
            @Override
            protected void bindThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends Filter> filterRegister, Map<String, String> initParams) {
                jeeFilterThrough(index, pattern, matcher, filterRegister, initParams);
            }
        };
    }

    @Override
    public FilterBindingBuilder<Filter> jeeFilterRegex(final String[] regexes) throws NullPointerException {
        List<String> uriPatterns = checkEmpty(Arrays.asList(regexes), "Filter patterns is empty.");
        return new FiltersModuleBinder<>(Filter.class, UriPatternType.REGEX, uriPatterns) {
            @Override
            protected void bindThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends Filter> filterRegister, Map<String, String> initParams) {
                jeeFilterThrough(index, pattern, matcher, filterRegister, initParams);
            }
        };
    }

    /** Filter 转换为 InvokerFilter */
    protected void jeeFilterThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends Filter> filterRegister, Map<String, String> initParams) {
        if (!this.isSingleton(filterRegister)) {
            throw new IllegalStateException("Filter must be Singleton.");
        }
        J2eeFilterAsFilter filterAsFilter = new J2eeFilterAsFilter(this.getProvider(filterRegister));
        BindInfo<J2eeFilterAsFilter> bindInfo = bindType(J2eeFilterAsFilter.class).uniqueName().toInstance(filterAsFilter).toInfo();
        this.filterThrough(index, pattern, matcher, bindInfo, initParams);
    }

    protected void filterThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends InvokerFilter> bindInfo, Map<String, String> initParams) {
        if (!this.isSingleton(bindInfo)) {
            throw new IllegalStateException("InvokerFilter must be Singleton.");
        }
        Supplier<AppContext> appContext = getProvider(AppContext.class);
        FilterDef define = new FilterDef(index, matcher, initParams, bindInfo, appContext);
        bindType(FilterDef.class).uniqueName().toInstance(define);
    }

    // ------------------------------------------------------------------------------------------------------
    private abstract class FiltersModuleBinder<T> implements FilterBindingBuilder<T> {
        private final Class<T>       targetType;
        private final UriPatternType uriPatternType;
        private final List<String>   uriPatterns;

        FiltersModuleBinder(Class<T> targetType, final UriPatternType uriPatternType, final List<String> uriPatterns) {
            this.targetType = targetType;
            this.uriPatternType = uriPatternType;
            this.uriPatterns = uriPatterns;
        }

        @Override
        public void through(final int index, final Class<? extends T> filterKey, final Map<String, String> initParams) {
            BindInfo<T> filterRegister = bindType(targetType).uniqueName().to(filterKey).toInfo();
            this.through(index, filterRegister, initParams);
        }

        @Override
        public void through(final int index, final T filter, final Map<String, String> initParams) {
            BindInfo<T> filterRegister = bindType(targetType).uniqueName().toInstance(filter).toInfo();
            this.through(index, filterRegister, initParams);
        }

        @Override
        public void through(final int index, final Supplier<? extends T> filterProvider, Map<String, String> initParams) {
            BindInfo<T> filterRegister = bindType(targetType).uniqueName().toProvider(filterProvider).asEagerSingleton().toInfo();
            this.through(index, filterRegister, initParams);
        }

        @Override
        public void through(int index, BindInfo<? extends T> filterRegister, Map<String, String> initParams) {
            if (initParams == null) {
                initParams = new HashMap<>();
            }
            for (String pattern : this.uriPatterns) {
                UriPatternMatcher matcher = UriPatternType.get(this.uriPatternType, pattern);
                this.bindThrough(index, pattern, matcher, filterRegister, initParams);
            }
        }

        protected abstract void bindThrough(int index, String pattern, UriPatternMatcher matcher, BindInfo<? extends T> filterRegister, Map<String, String> initParams);
    }
    // ------------------------------------------------------------------------------------------------------

    /** HttpServlet 转换为 MappingTo 形式 */
    protected void jeeServlet(int index, String pattern, BindInfo<? extends HttpServlet> servletRegister, Map<String, String> initParams) {
        if (!this.isSingleton(servletRegister)) {
            throw new IllegalStateException("HttpServlet must be Singleton.");
        }
        OneConfig oneConfig = new OneConfig(servletRegister.getBindID(), initParams, getProvider(AppContext.class));
        Supplier<? extends Servlet> j2eeServlet = getProvider(servletRegister);
        mappingTo(pattern).with(index, new J2eeServletAsMapping(oneConfig, j2eeServlet));
    }

    @Override
    public ServletBindingBuilder jeeServlet(final String[] morePatterns) {
        return new ServletsModuleBuilder(checkEmpty(Arrays.asList(morePatterns), "Servlet patterns is empty."));
    }

    private class ServletsModuleBuilder implements ServletBindingBuilder {
        private final List<String> uriPatterns;

        ServletsModuleBuilder(List<String> uriPatterns) {
            this.uriPatterns = uriPatterns;
        }

        @Override
        public void with(final int index, final Class<? extends HttpServlet> servletKey, final Map<String, String> initParams) {
            BindInfo<HttpServlet> servletRegister = bindType(HttpServlet.class).uniqueName().to(servletKey).toInfo();
            this.with(index, servletRegister, initParams);
        }

        @Override
        public void with(final int index, final HttpServlet servlet, final Map<String, String> initParams) {
            BindInfo<HttpServlet> servletRegister = bindType(HttpServlet.class).uniqueName().toInstance(servlet).toInfo();
            this.with(index, servletRegister, initParams);
        }

        @Override
        public void with(final int index, final Supplier<? extends HttpServlet> servletProvider, Map<String, String> initParams) {
            BindInfo<HttpServlet> servletRegister = bindType(HttpServlet.class).uniqueName().toProvider(servletProvider).asEagerSingleton().toInfo();
            this.with(index, servletRegister, initParams);
        }

        @Override
        public void with(int index, BindInfo<? extends HttpServlet> servletRegister, Map<String, String> initParams) {
            if (initParams == null) {
                initParams = new HashMap<>();
            }
            for (String pattern : this.uriPatterns) {
                jeeServlet(index, pattern, servletRegister, initParams);
            }
            logger.info(String.format("mapingTo[Servlet] -> bindID '%s' mappingTo: '%s'.", servletRegister.getBindID(), this.uriPatterns));
        }
    }
    // ------------------------------------------------------------------------------------------------------

    @Override
    public List<Mapping> getMappings() {
        return List.copyOf(this.mappings);
    }

    @Override
    public <T> MappingToBindingBuilder<T> mappingTo(String[] morePatterns) {
        checkEmpty(Arrays.asList(morePatterns), "mappingTo patterns is empty.");
        return new MappingToBindingBuilder<T>() {
            @Override
            public void with(int index, Class<? extends T> targetKey) {
                this.with(index, bindType(targetKey).uniqueName().toInfo());
            }

            @Override
            public void with(int index, T target) {
                Class<T> targetType = (Class<T>) target.getClass();
                this.with(index, bindType(targetType).uniqueName().toInstance(target).toInfo());
            }

            @Override
            public void with(int index, Class<T> referKey, Supplier<? extends T> targetProvider) {
                this.with(index, bindType(referKey).uniqueName().toProvider(targetProvider).toInfo());
            }

            @Override
            public void with(int index, BindInfo<? extends T> targetInfo) {
                Arrays.stream(morePatterns).filter(StringUtils::isNotBlank).forEach(pattern -> {
                    MappingDef define = new MappingDef(index, targetInfo, pattern, Matchers.anyMethod(), true);
                    mappings.add(define);
                    bindType(MappingDef.class).uniqueName().toInstance(define);
                });
                logger.info(String.format("mapingTo[%s] -> bindType '%s' mappingTo: '%s'.", targetInfo.getBindID(), targetInfo.getBindType(), Arrays.toString(morePatterns)));
            }
        };
    }
    // ------------------------------------------------------------------------------------------------------

    /** 拦截这些后缀的请求，这些请求会被渲染器渲染。 */
    public WebApiBinder.RenderEngineBindingBuilder addRender(String renderName) {
        return new RenderEngineBindingBuilderImpl(Objects.requireNonNull(renderName, "Render renderName is empty.")) {
            @Override
            protected void bindRender(String renderName, BindInfo<? extends RenderEngine> bindInfo) {
                bindType(RenderDef.class).nameWith(renderName).toInstance(new RenderDef(renderName, bindInfo));
            }
        };
    }
    // ------------------------------------------------------------------------------------------------------

    private abstract class RenderEngineBindingBuilderImpl implements WebApiBinder.RenderEngineBindingBuilder {
        private final String renderName;

        public RenderEngineBindingBuilderImpl(String renderName) {
            this.renderName = renderName;
        }

        @Override
        public <T extends RenderEngine> void to(Class<T> renderEngineType) {
            bindRender(this.renderName, bindType(RenderEngine.class).uniqueName().to(renderEngineType).toInfo());
        }

        @Override
        public void toProvider(Supplier<? extends RenderEngine> renderEngineProvider) {
            bindRender(this.renderName, bindType(RenderEngine.class).uniqueName().toProvider(renderEngineProvider).toInfo());
        }

        @Override
        public void bindToInfo(BindInfo<? extends RenderEngine> renderEngineInfo) {
            bindRender(this.renderName, Objects.requireNonNull(renderEngineInfo));
        }

        protected abstract void bindRender(String renderName, BindInfo<? extends RenderEngine> bindInfo);
    }
}
