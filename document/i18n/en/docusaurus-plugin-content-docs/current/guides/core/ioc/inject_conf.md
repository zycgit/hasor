---
id: configioc
sidebar_position: 7
title: 2.1.6 Injecting Configuration
description: Inject values from Hasor configuration into beans.
---

# 2.1.6 Injecting Configuration

The following example uses a properties file:

```properties
jdbcSettings.jdbcDriver   = com.mysql.jdbc.Driver
jdbcSettings.jdbcURL      = jdbc:mysql://127.0.0.1:3306/test
jdbcSettings.userName     = sa
jdbcSettings.userPassword =
```

The same configuration can also be expressed in XML:

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

In a bean, use the `net.hasor.core.InjectSettings` annotation to indicate that the injected value comes from the configuration file.

```java title='Example'
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

Finally, specify the configuration file to load when creating the container.

```java
AppContext appContext = Hasor.create().mainSettingWith("<config-file-name>").build();
```

## Automatic Type Conversion

`@InjectSettings` can perform simple type conversion. The conversion utility is `net.hasor.cobble.convert.ConverterUtils`.

```java
public class TestBean {
    @InjectSettings("userInfo.myAge")
    private int myAge;
}
```
