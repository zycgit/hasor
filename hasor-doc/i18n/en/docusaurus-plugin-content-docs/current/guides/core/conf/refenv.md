---
id: refenv
sidebar_position: 5
title: d. Referencing External Parameters
description: Use placeholders to reference JVM and environment values in Hasor configuration.
---

# Referencing External Parameters

Use database connection configuration as an example:

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

If you want to separate the database account and password from the configuration file, use `${KEY}` placeholders in configuration values. Placeholders are read from JVM `-D` parameters or operating-system environment variables when configuration is loaded.

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

Pass values in either of the following ways during startup:

```bash
java -DJDBC_USER_NAME=sa -DJDBC_USER_PWD=password -jar app.jar
```

You can also write settings through code before startup:

```java
AppContext appContext = Hasor.create()
        .addSettings(Settings.DefaultNameSpace, "jdbcSettings.userName", "sa")
        .addSettings(Settings.DefaultNameSpace, "jdbcSettings.userPassword", "password")
        .mainSettingWith("hconfig.xml")
        .build();
```
