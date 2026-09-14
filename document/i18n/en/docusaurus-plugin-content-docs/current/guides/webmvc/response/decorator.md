---
id: decorator
sidebar_position: 3
title: 4.4.3 Layout Templates
description: Use layout-template technology to decorate rendered pages.
---

# 4.4.3 Layout Templates

:::tip
Further reading: Sitemesh is a framework focused on layout templates.
:::

Layout-template technology is mature. Its working principle is a typical decorator pattern. When a decorated page is rendered, the target page is first rendered into a temporary buffer.

Then the layout page is rendered again. At that point, the pre-rendered page is inserted into a specific position in the layout page. Finally, the combined new page is returned to the browser.

:::caution
During one page request in Hasor, only one layout page takes effect. Nested layout templates are not supported.

If common layout sections need to be extracted inside a layout template, modularize the layout-template file content instead of applying nested layouts.
:::

By default, Hasor's layout-template capability is disabled. It can be enabled through configuration. When enabling it, it is best to specify the resource-file locations for the layout pages and regular pages:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/hasor-web">
    <hasor>
        <layout enable="true" placeholder="content_placeholder" defaultLayout="default.html">
            <!-- Layout page resource location. Optional; default: /layout. -->
            <layoutPath>/layout</layoutPath>
            <!-- Page resource location. Optional; default: /templates. -->
            <templatePath>/templates</templatePath>
        </layout>
    </hasor>
</config>
```

With the default configuration above, the web project directory structure roughly becomes:

```text
webapp
    layout      Layout pages, specified by hasor.layout.layoutPath.
    templates   Site pages, specified by hasor.layout.templatePath.
    control     Page modules, optional; stores repeated templates in pages.
    static      Static resource files, optional.
    WEB-INF     web.xml
```

## Adding a Footer

Assume all pages on a site need a unified footer. First, create the layout page and save it as `/webapp/layout/default.html`.

```html
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${rootData.pageTitle!}</title>
    </head>
    <body>
        ${content_placeholder!}
        <div>this is foot</div>
    </body>
</html>
```

The variables in this layout page mean:

| Variable             | Meaning                                                        |
|----------------------|----------------------------------------------------------------|
| content_placeholder  | The actual target page content visited by the user             |
| rootData             | A global request-scoped object used by the request handler to store data |

Then create the target page and save it as `/webapp/templates/target.html`.

```html
${rootData.put('pageTitle','Home')}
<p>this page form user</p>
```

Finally, start the web container and visit `http://localhost:8080/target.html` to see the complete result:

```html
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Home</title>
    </head>
    <body>
        <p>this page form user</p>
        <div>this is foot</div>
    </body>
</html>
```
