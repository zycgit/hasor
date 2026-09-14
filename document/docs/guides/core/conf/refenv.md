---
id: refenv
sidebar_position: 5
title: 2.8.4 引用外部参数
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 2.8.4 引用外部参数

以配置数据库链接配置作为例子：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <jdbcSettings>
        <jdbcDriver>com.mysql.jdbc.Driver</jdbcDriver>
        <userName>sa</userName>
        <userPassword>password</userPassword>
    </jdbcSettings>
</config>
```

如果想把数据库连接的帐号和密码剥离出来，可以在配置值中使用 `${KEY}` 占位符。占位符会在配置加载时从 JVM `-D` 参数或操作系统环境变量中读取。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <jdbcSettings>
        <jdbcDriver>com.mysql.jdbc.Driver</jdbcDriver>
        <userName>${JDBC_USER_NAME:sa}</userName>
        <userPassword>${JDBC_USER_PWD:password}</userPassword>
    </jdbcSettings>
</config>
```

启动时可以通过下面任意一种方式传入：

```bash
java -DJDBC_USER_NAME=sa -DJDBC_USER_PWD=password -jar app.jar
```

也可以使用代码方式在启动前写入 Settings：

```java
AppContext appContext = Hasor.create()
        .addSettings(Settings.DefaultNameSpace, "jdbcSettings.userName", "sa")
        .addSettings(Settings.DefaultNameSpace, "jdbcSettings.userPassword", "password")
        .mainSettingWith("hconfig.xml")
        .build();
```
