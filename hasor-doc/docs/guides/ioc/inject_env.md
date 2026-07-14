---
id: envioc
sidebar_position: 8
title: g.注入外部配置
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 注入外部配置

`@InjectSettings` 可以注入配置项。配置文件中的值支持 `${KEY}` 占位符，因此敏感信息可以通过 JVM `-D` 参数或操作系统环境变量传入。

配置文件：

```properties
db.user = ${DB_USER}
db.pwd  = ${DB_PWD}
```

Bean：

```java
public class DataBaseBean {
    @InjectSettings("db.user")
    private String user;

    @InjectSettings("db.pwd")
    private String password;
}
```

启动程序时传入参数：

```bash
java -DDB_USER=username -DDB_PWD=password -jar app.jar
```

`@InjectSettings("${db.user}")` 也会读取名为 `db.user` 的 Settings 配置项。这个写法适合需要把配置项名保持在占位符形式的场景。

如果配置项不存在，可以在注解中设置默认值：

```java
public class DataBaseBean {
    @InjectSettings(value = "db.poolSize", defaultValue = "8")
    private int poolSize;
}
```
