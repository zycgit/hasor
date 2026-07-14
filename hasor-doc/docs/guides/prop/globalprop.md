---
id: globalprop
sidebar_position: 4
title: c.全局动态属性
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 全局动态属性

为每个 Bean 单独设置动态属性会比较麻烦，因此可以通过全局方式统一设置动态属性。用法和设置 Aop 有点类似。

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
apiBinder.dynamicProperty(
        t -> true,	// 匹配的类，类型为：Predicate<Class<?>>
        "name",		// 属性名
        String.class// 属性类型
	);
});
```

然后创建 Bean 就可以通过反射的形式调用对应的 get/set 了。

```java
// 创建Bean
PojoBean pojoBean = appContext.getInstance(PojoBean.class);

// 获取 get/set方法
Method getMethod = pojoBean.getClass().getMethod("getName");
Method setMethod = pojoBean.getClass().getMethod("setName", String.class);

// 反射的方式注入 name 属性
setMethod.invoke(pojoBean, "Hello");

// 反射方式获取 name 属性
System.out.println("data = " + getMethod.invoke(pojoBean));
```
