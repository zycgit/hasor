---
id: modules
sidebar_position: 3
title: 6.3 Modular Configuration
description: Split Hasor configuration files across multiple projects and merge them at startup.
---

# 6.3 Modular Configuration

:::tip
In Hasor, one Java project can contain multiple subprojects, and each subproject can be an independent jar or war. Each subproject can also define its own `hconfig` configuration file.

When the project starts, these configuration files scattered across different subprojects are gathered back together. This is Hasor's modular configuration-file mechanism.
:::

The most typical use case for modular configuration files is a multi-project application.

## Steps

- Create a multi-project Java project.
- Create a `META-INF` directory under the classpath of each project.
- Create a `hasor.schemas` file under the `META-INF` directory.
- Write the classpath location of the `hconfig.xml` file into it. For example, `hasor-core` itself uses the following `hasor.schemas` content:

```text
/META-INF/hasor-framework/core-hconfig.xml
```

A project can contain multiple `hconfig` files. Write one configuration-file path per line.

## Loading Principle

When Hasor starts, it scans all `hasor.schemas` files in jars, gathers and deduplicates the listed configuration files, and then loads them one by one. Finally, it wraps them as the `net.hasor.cobble.setting.Settings` interface.

![](../_img/CC2_8633_6D5C_MK4L.png)

## Namespaces

After configuration files are split into multiple modules, the same configuration may exist in different files. This can cause configuration conflicts, so XML namespaces should be used for isolation.

Use XML namespaces to isolate different configuration blocks. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://mode1.myProject.net"><!-- XML namespace isolation. -->
    <serverLocal url="www.126.com" />
</config>

<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://mode2.myProject.net"><!-- XML namespace isolation. -->
    <serverLocal url="www.souhu.com" />
</config>
```
