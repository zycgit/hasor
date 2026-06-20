---
id: render
sidebar_position: 1
title: a. Renderers
description: Render response data into visible view content.
---

# Renderers

A Hasor renderer is essentially a view-layer component. Its main purpose is to help an application render data into a visible form. A typical scenario is that a request handler produces data after execution and passes it to JSP to generate an HTML page.

Here, JSP is the rendering engine, which is the same concept as a renderer in Hasor. A renderer must come from the `net.hasor.web.render.RenderEngine` interface.

For example, after a request is processed, it needs to be rendered into HTML with Freemarker. In that case, a renderer is needed:

```java
// The renderer name is flt.
@Render("flt")
public class FreemarkerRender implements RenderEngine {
    protected Configuration freemarker;

    public void initEngine(AppContext appContext) throws Throwable {
        // Initialization runs only once. Initialize freemarker here.
        this.freemarker = ...
    }

    public boolean exist(String template) throws IOException {
        // Indicates whether the renderer should return rendering to the Servlet container.
        // If the renderer does not plan to handle this view, return false.
        //  - If the template does not exist, return it to the Servlet container.
        return freemarker.getTemplateLoader().findTemplateSource(template) != null;
    }

    public void process(RenderInvoker renderData, Writer writer) throws Throwable {
        // Execute Freemarker rendering.
        Template temp = this.freemarker.getTemplate(renderData.renderTo());
        HashMap<String, Object> data = new HashMap<>();
        renderData.forEach(data::put);
        temp.process(data, writer);
    }
}
```

After a renderer is written, it must be registered in the framework.

```java
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        // Scan all classes annotated with @Render.
        Set<Class<?>> classSet = apiBinder.findClass(Render.class, "com.example.web.render.*");
        // Configure renderers.
        apiBinder.loadRender(classSet);
    }
}
```

Finally, specify the concrete renderer in the request handler.

```java
@MappingTo("/my.html")
public class HtmlProduces {
    @Any
    public void testProduces1() {
        invoker.renderTo("flt", "/my.flt");
    }
}
```
