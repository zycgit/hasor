---
id: convert
sidebar_position: 7
title: 4.3.7 Automatic Parameter Type Conversion
description: Convert request parameter values to common Java types automatically.
---

# 4.3.7 Automatic Parameter Type Conversion

Hasor Web can help perform simple type conversion. Supported types include:
- Basic types: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `String`
- Large number types: `BigInteger`, `BigDecimal`
- Date and time types: `java.util.Date`, `java.util.Calendar`, `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`
- Other types: `Enum`, `File`, `URL`, `URI`

```java
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@RequestParameter("name") String name,
            @RequestParameter("age") int age) {
        ...
    }
}
```

:::tip
Type conversion uses the `net.hasor.cobble.convert.ConverterUtils` utility. Configure date formats through `ConverterUtils` with the code below.

Executing it once during application startup is enough.
:::
