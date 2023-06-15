---
id: refenv
sidebar_position: 5
title: d.引用环境变量
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 引用环境变量

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

如果想把数据库连接的帐号和密码剥离出来，并且以系统环境变量或者 -D 参数传入。

可以先把帐号和密码剥离到环境变量中，接着就可以使用前面章节提到的通过系统环境变量来传入：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.environmentVar>
        <JDBC_USER_NAME>sa</JDBC_USER_NAME>
        <JDBC_USER_PWD>password</JDBC_USER_PWD>
    </hasor.environmentVar>

    <jdbcSettings>
        <jdbcDriver>com.mysql.jdbc.Driver</jdbcDriver>
        <userName>${JDBC_USER_NAME}</userName>
        <userPassword>${JDBC_USER_PWD}</userPassword>
    </jdbcSettings>
</config>
```

然后在 jvm 启动时候将 `JDBC_USER_NAME` 和 `JDBC_USER_PWD` 动态的传入给应用程序。