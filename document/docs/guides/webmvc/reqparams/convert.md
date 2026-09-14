---
id: convert
sidebar_position: 7
title: 4.3.7 参数自动类型转换
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.3.7 参数自动类型转换

Hasor Web 框架可以帮助你进行简单的类型转换，可以转换的类型有：
- 基础类型：`byte`、`short`、`int`、`long`、`float`、`double`、`boolean`、`String`
- 大数类型：`BigInteger`、`BigDecimal`
- 时间日期：`java.util.Date`、`java.util.Calendar`、`java.sql.Date`、`java.sql.Time`、`java.sql.Timestamp`
- 其它类型：`Enum`、`File`、`URL`、`URI`

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
类型转换是使用的 `net.hasor.cobble.convert.ConverterUtils` 工具，因此设置时间格式需要通过下面这段代码来配置 `ConverterUtils` 工具。

整个程序启动时执行一次就可以。
:::
