---
id: xml
sidebar_position: 4
title: XML 格式差异性
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# XML 格式差异性

XML 格式是能力最为全面的格式，较前两种方案只有在命令行界面下维护和编辑比较不太方便。诸如多个配置文件的配置内容隔离、数组类型，XML都能很好的支持。

## 原理

Hasor 的配置文件解析机制类似 XPath，每一个元素和元素上的属性都是一个节点。节点和节点之间使用 “.” 来分割。

```text title='局限性'
- 它没有 XPath 表达式那样强大，主要是当遇到XPath 路径上某节点出现多个同名元素时。不能有效的直接获取所有数据。
- 其次，如果 xml 元素属性中某个属性的名字和子元素名字出现冲突的话也无法有效的使用上述简单的方式来获取它们。
```

例如：有如下配置文件，读取它的内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <!-- Demo 项目源码所处包 -->
    <hasor debug="false">
        <loadPackages>net.test.project.*</loadPackages>
    </hasor>
    <hasor-jdbc>
        <!-- 名称为 localDB 的内存数据库，数据库引擎使用 HSQL -->
        <dataSource name="localDB" dsFactory="net.test.C3p0Factory">
            <driver>org.hsqldb.jdbcDriver</driver>
            <url>jdbc:hsqldb:mem:aname</url>
        </dataSource>
    </hasor-jdbc>
</config>
```

- 如果想读取：`debug="false"` 对应的表达式为：`hasor.debug`
- 如果想读取：`net.test.project.*` 对应的表达式为：`hasor.loadPackages`
- 如果想读取：`org.hsqldb.jdbcDriver` 对应的表达式为：`hasor-jdbc.dataSource.driver`

## 冲突的情况

这种情况我们要尽量回避，以避免带来麻烦。举例Xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor debug="false">
        <debug>true</debug>
    </hasor>
</config>
```

配置文件中 hasor 节点下面有两个节点，一个是元素节 debug，一个是属性节点 debug。根据 Hasor 配置文件的读取规则，这两个节点的最终表达式均为：`hasor.debug`。

这种情况j就是配置就冲突。在冲突的情况下默认可以获取到的是最后一次配置的值，但如果想获取所有配置就要通过 `getBooleanArray` 方法获取。例如：

```java
Boolean debugValue = settings.getBoolean("hasor.debug");
// 返回值为：true
Boolean[] debugArrays = settings.getBooleanArray("hasor.debug");
// 返回值为：[false,true]
```

:::caution
如果要解析类似上述场景的 Xml 配置文件，建议通过内置的 Xml方式进行解析。
:::

## 命名空间

:::tip
Hasor 中配置文件的命名空间概念等同于 Xml 中的命名空间，其存在和设立的意义完全是为了传承 namespace 的概念。

在 Hasor 中使用命名空间最典型的场景是为不同模块隔离配置。当前 Hasor 仓库主要保留 `hasor-core` 和 `hasor-web` 两类配置命名空间。
:::

首先存在一个带有多个命名空间的 Xml 配置文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns:mod1="http://mode1.myProject.net"
        xmlns:mod2="http://mode2.myProject.net"
        xmlns="http://www.hasor.net/sechma/main">

    <!-- mode1.myProject.net 配置 -->
    <mod1:config>
        <mod1:serverLocal mod1:url="www.126.com" />
    </mod1:config>

    <!-- http://mode2.myProject.net 配置 -->
    <mod2:config>
        <mod2:serverLocal mod2:url="www.souhu.com" />
    </mod2:config>
</config>
```

然后在读取配置内容时候可以通过不需要明确配置的来源：

```java
// 创建 Hasor 容器，并且获取到配置文件接口
AppContext appContext = Hasor.create().build();
Settings defaultSettings = appContext.getInstance(Settings.class);

// 获取 mode1 空间下的配置
Settings mod1Settings = defaultSettings.getSettings("http://mode1.myProject.net");
// 获取 mode2 空间下的配置
Settings mod2Settings = defaultSettings.getSettings("http://mode2.myProject.net");

// 读取各自空间的配置内容
String url1 = mod1Settings.getString("serverLocal.url");
String url2 = mod2Settings.getString("serverLocal.url");
```

## 默认命名空间

`http://www.hasor.net/sechma/main` 空间是保留给应用的，如没有特殊的需求。建议使用这个空间。例如：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    ...
</config>
```

默认配置空间在一些场景下享有优先权。例如：在读取配置的时候会优先尝试在 `http://www.hasor.net/sechma/main` 中读取，当读取不到到时候才会在其它命名空间中查找。
- 按照字符串排序顺序决定，命名空间的索引顺序。
- 所有 Hasor 官方出品，基于 Hasor 体系构建的框架，其配置空间都在 `http://www.hasor.net/sechma/` 下进行定义。

下面是当前 Hasor 文档覆盖的配置空间表：

| 模块      | 命名空间                                      | 说明 |
|---------|-------------------------------------------|------|
| 应用自身    | `http://www.hasor.net/sechma/main`         | 应用自己的默认配置空间 |
| Core    | `http://www.hasor.net/sechma/hasor-core`   | `hasor-core` 的基础容器、模块、SPI、配置能力 |
| Web     | `http://www.hasor.net/sechma/hasor-web`    | `hasor-web` 的 Web MVC、文件上传、内嵌 HTTP 配置 |
| web-mime | `http://www.hasor.net/sechma/mime-mapping` | Web MIME 映射配置 |
