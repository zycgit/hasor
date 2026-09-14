---
id: xml
sidebar_position: 4
title: 2.8.3 XML Format Differences
description: XML configuration features, paths, conflicts, and namespaces.
---

# 2.8.3 XML Format Differences

XML is the most capable configuration format. Compared with the first two formats, it is only less convenient to maintain and edit from a command-line interface. XML supports configuration isolation across multiple files and array types very well.

## Principle

Hasor's configuration parsing mechanism is similar to XPath. Every element and every element attribute is treated as a node. Nodes are separated with `.`.

```text title='Limitations'
- It is not as powerful as XPath expressions. In particular, when multiple elements with the same name appear on an XPath path, it cannot directly and effectively retrieve all data.
- Also, if an XML element attribute name conflicts with a child element name, the simple access method described above cannot effectively retrieve both values.
```

For example, consider the following configuration file and how to read its content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <!-- Package where the demo project source code is located. -->
    <hasor debug="false">
        <loadPackages>net.test.project.*</loadPackages>
    </hasor>
    <hasor-jdbc>
        <!-- An in-memory database named localDB, using HSQL as the database engine. -->
        <dataSource name="localDB" dsFactory="net.test.C3p0Factory">
            <driver>org.hsqldb.jdbcDriver</driver>
            <url>jdbc:hsqldb:mem:aname</url>
        </dataSource>
    </hasor-jdbc>
</config>
```

- To read `debug="false"`, use the expression `hasor.debug`.
- To read `net.test.project.*`, use the expression `hasor.loadPackages`.
- To read `org.hsqldb.jdbcDriver`, use the expression `hasor-jdbc.dataSource.driver`.

## Conflicts

Avoid this situation whenever possible because it causes trouble. Example XML:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor debug="false">
        <debug>true</debug>
    </hasor>
</config>
```

Under the `hasor` node, the configuration file contains two nodes: an element node named `debug` and an attribute node named `debug`. According to Hasor's configuration-reading rules, both nodes produce the same final expression: `hasor.debug`.

This is a configuration conflict. By default, the last configured value is obtained. To obtain all configured values, use a method such as `getBooleanArray`. For example:

```java
Boolean debugValue = settings.getBoolean("hasor.debug");
// Return value: true.
Boolean[] debugArrays = settings.getBooleanArray("hasor.debug");
// Return value: [false,true].
```

:::caution
If you need to parse XML configuration files like the scenario above, use the built-in XML parser.
:::

## Namespaces

:::tip
The namespace concept in Hasor configuration files is equivalent to XML namespaces. It exists to inherit the namespace concept.

A typical namespace scenario in Hasor is separating configuration for different modules. The current Hasor repository mainly keeps the `hasor-core` and `hasor-web` namespaces.
:::

First, define an XML configuration file with multiple namespaces:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns:mod1="http://mode1.myProject.net"
        xmlns:mod2="http://mode2.myProject.net"
        xmlns="http://www.hasor.net/sechma/main">

    <!-- Configuration for mode1.myProject.net. -->
    <mod1:config>
        <mod1:serverLocal mod1:url="www.126.com" />
    </mod1:config>

    <!-- Configuration for http://mode2.myProject.net. -->
    <mod2:config>
        <mod2:serverLocal mod2:url="www.souhu.com" />
    </mod2:config>
</config>
```

Then, when reading configuration content, you can select the source namespace explicitly:

```java
// Create the Hasor container and obtain the configuration-file interface.
AppContext appContext = Hasor.create().build();
Settings defaultSettings = appContext.getInstance(Settings.class);

// Get configuration under the mode1 namespace.
Settings mod1Settings = defaultSettings.getSettings("http://mode1.myProject.net");
// Get configuration under the mode2 namespace.
Settings mod2Settings = defaultSettings.getSettings("http://mode2.myProject.net");

// Read each namespace's configuration content.
String url1 = mod1Settings.getString("serverLocal.url");
String url2 = mod2Settings.getString("serverLocal.url");
```

## Default Namespace

The `http://www.hasor.net/sechma/main` namespace is reserved for applications. If there is no special requirement, this namespace is recommended. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    ...
</config>
```

The default configuration namespace has priority in some scenarios. For example, when reading configuration, Hasor first attempts to read from `http://www.hasor.net/sechma/main`; if it cannot find the value, it searches other namespaces.
- Namespace index order is determined by string sort order.
- All official Hasor frameworks built on the Hasor system define their configuration namespaces under `http://www.hasor.net/sechma/`.

The following table lists the namespaces covered by the current Hasor documentation:

| Module      | Namespace                                    | Description |
|-------------|----------------------------------------------|-------------|
| Application | `http://www.hasor.net/sechma/main`           | Application default namespace |
| Core        | `http://www.hasor.net/sechma/hasor-core`     | Core container, modules, SPI, and configuration |
| Web         | `http://www.hasor.net/sechma/hasor-web`      | Web MVC, file upload, and embedded HTTP settings |
| web-mime    | `http://www.hasor.net/sechma/mime-mapping`   | Web MIME mapping configuration |
