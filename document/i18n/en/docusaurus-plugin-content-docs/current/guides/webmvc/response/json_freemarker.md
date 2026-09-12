---
id: freemarker
sidebar_position: 5
title: FreeMarker Rendering Engine
description: Implement a FreeMarker rendering engine for Hasor Web.
---

# FreeMarker Rendering Engine

```java
/**
 * Freemarker renderer.
 * @version : January 3, 2016
 * @author Yongchun Zhao (zyc@hasor.net)
 */
@Render("flt")
public class FreemarkerRender implements RenderEngine {
    protected Configuration freemarker;

    /** Built-in method for creating the Freemarker object. You can also set your own object through apiBinder.bind(Configuration.class).... */
    protected Configuration newConfiguration(AppContext appContext, ServletContext servletContext) throws IOException {
        String realPath = servletContext.getRealPath("/");
        TemplateLoader templateLoader = new FileTemplateLoader(new File(realPath), true);
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setTemplateLoader(templateLoader);

        String responseEncoding = appContext.findBindingBean(RuntimeFilter.HTTP_RESPONSE_ENCODING_KEY, String.class);
        if (StringUtils.isBlank(responseEncoding)) {
            responseEncoding = Settings.DefaultCharset;
        }
        configuration.setDefaultEncoding(responseEncoding);
        configuration.setOutputEncoding(responseEncoding);
        configuration.setLocalizedLookup(false); // Whether to enable internationalization: false.
        configuration.setClassicCompatible(true); // Configuration for null-value handling.

        return configuration;
    }

    /** Various utilities and variables. */
    protected void configSharedVariable(AppContext appContext, ServletContext servletContext, Configuration freemarker)
            throws TemplateModelException {
        freemarker.setSharedVariable("stringUtils", new StringUtils());
        freemarker.setSharedVariable("ctx_path", servletContext.getContextPath());
    }

    public void initEngine(AppContext appContext) throws Throwable {
        ServletContext servletContext = appContext.getInstance(ServletContext.class);
        if (servletContext == null) {
            throw new IllegalStateException("ServletContext is required.");
        }
        BindInfo<Configuration> bindInfo = appContext.getBindInfo(Configuration.class);
        if (bindInfo == null) {
            this.freemarker = this.newConfiguration(appContext, servletContext);
        } else {
            this.freemarker = appContext.getInstance(bindInfo);
        }
        if (this.freemarker == null) {
            throw new IllegalStateException("Freemarker Configuration is required.");
        }
        this.configSharedVariable(appContext, servletContext, this.freemarker);
    }

    public boolean exist(String template) throws IOException {
        return this.freemarker.getTemplateLoader().findTemplateSource(template) != null;
    }

    public void process(RenderInvoker renderData, Writer writer) throws Throwable {
        Template temp = this.freemarker.getTemplate(renderData.renderTo());
        if (temp == null) {
            return;
        }
        HashMap<String, Object> data = new HashMap<>();
        for (String key : renderData.keySet()) {
            data.put(key, renderData.get(key));
        }
        temp.process(data, writer);
    }
}
```
