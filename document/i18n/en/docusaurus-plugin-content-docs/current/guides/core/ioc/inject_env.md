---
id: envioc
sidebar_position: 8
title: 2.1.7 Injecting External Configuration
description: Inject external values through Settings placeholders.
---

# 2.1.7 Injecting External Configuration

`@InjectSettings` can inject configuration items. Values in the configuration file support `${KEY}` placeholders, so sensitive information can be provided through JVM `-D` parameters or operating-system environment variables.

Configuration file:

```properties
db.user = ${DB_USER}
db.pwd  = ${DB_PWD}
```

Bean:

```java
public class DataBaseBean {
    @InjectSettings("db.user")
    private String user;

    @InjectSettings("db.pwd")
    private String password;
}
```

Pass parameters when starting the program:

```bash
java -DDB_USER=username -DDB_PWD=password -jar app.jar
```

`@InjectSettings("${db.user}")` also reads the `Settings` item named `db.user`. This form is useful when the configuration key itself needs to remain in placeholder form.

If a configuration item does not exist, set a default value on the annotation:

```java
public class DataBaseBean {
    @InjectSettings(value = "db.poolSize", defaultValue = "8")
    private int poolSize;
}
```
