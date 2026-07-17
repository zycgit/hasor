---
id: configioc
sidebar_position: 7
title: f.注入配置
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 注入配置

下面以属性文件为例：

```properties
jdbcSettings.jdbcDriver   = com.mysql.jdbc.Driver
jdbcSettings.jdbcURL      = jdbc:mysql://127.0.0.1:3306/test
jdbcSettings.userName     = sa
jdbcSettings.userPassword =
```

也可以通过 Xml 文件来表示相同的配置内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <jdbcSettings>
        <jdbcDriver>com.mysql.jdbc.Driver</jdbcDriver>
        <jdbcURL>jdbc:mysql://127.0.0.1:3306/test</jdbcURL>
        <userName>sa</userName>
        <userPassword></userPassword>
    </jdbcSettings>
</config>
```

在 Bean 中通过 `net.hasor.core.InjectSettings` 注解来表示注入的内容来自于配置文件

```java title='例如'
public class DataBaseBean {
    @InjectSettings("jdbcSettings.jdbcDriver")
    private String jdbcDriver;

    @InjectSettings("jdbcSettings.jdbcURL")
    private String jdbcURL;

    @InjectSettings("jdbcSettings.userName")
    private String user;

    @InjectSettings("jdbcSettings.userPassword")
    private String password;

    ...
}
```

最后在创建容器的时候指定要加载的配置文件即可。

```java
AppContext appContext = Hasor.create().mainSettingWith("<config-file-name>").build();
```

## 类型自动转换

`@InjectSettings` 可以帮助做一些简单的类型转换，类型转换工具为 `net.hasor.cobble.convert.ConverterUtils`。

```java
public class TestBean {
    @InjectSettings("userInfo.myAge")
    private int myAge;
}
```
