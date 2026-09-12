---
id: constructorioc
sidebar_position: 2
title: 构造方法注入
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 构造方法注入

当类中有且只有一个无参的构造方法时，是不需要通过 `@ConstructorBy` 来指明构建 `Bean` 的构造方法。

首先，在要被注入的构造方法上标记 `net.hasor.core.ConstructorBy` 注解，以表示在创建 `Bean` 的时候使用这个构造方法。

然后，如果有参数要被注入，那么在需要注入的参数前面加上 `net.hasor.core.Inject` 注解。以表示某个参数的来源是通过依赖注入进来的。

```java title='例如'
public class CustomBean {
    private FunBean funBean = null;

    @ConstructorBy
    public CustomBean(@Inject() FunBean funBean) {
        this.funBean = funBean;
    }

    public void callFoo() {
        this.funBean.foo();
    }
}
```

创建 Bean 也很简单，无需任何注册或者声明Bean的步骤。只需要从容器中按类型获取即可，Hasor 会自动在创建 Bean 过程中解析配置。

```java
AppContext appContext = Hasor.create().build();
CustomBean myBean = appContext.getInstance(CustomBean.class);
```

:::tip
`net.hasor.core.ConstructorBy` 注解和 `javax.inject.Inject` 注解具有相同功效。
如果类上出现多个 `ConstructorBy` 注解，那么将会按照构造方法参数个数排序。最后取参数最少的那个作为最终的构造方法。
:::
