---
id: valueprop
sidebar_position: 2
title: a.值型动态属性
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 值型动态属性

:::tip
值型是指，附加的动态属性只能简单的进行 get/set。其行为相当于类型多了一个私有字段而已
:::

在某些较难处理的代码逻辑中 “附加属性透传” 是一个很好的解决问题思路，利用动态属性可以在不修改原有类型的情况下，将一个附加的信息进行透传。例如：

```java
// 原始类
public class PojoBean {
    private String name;
    private int    age;

	// name 的 get/set
    public String getName() { ... }
    public void setName(String name) { ... }

	// age 的 get/set
    public int getAge() { ... }
    public void setAge(int age) { ... }
}

// 在不修改代码情况下增加一个属性 type，例如下面样子
public class PojoBean {
    private String name;
    private int    age;
    private int    type;

	// name 的 get/set
    public String getName() { ... }
    public void setName(String name) { ... }

	// age 的 get/set
    public int getAge() { ... }
    public void setAge(int age) { ... }

	// type 的 get/set
    public int getType() { ... }
    public void setType(int type) { ... }
}
```

首先有一个简单的 Bean。

```java
public class PojoBean {
    private String     uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
```

接着创建 Hasor 容器，然后注册这个 Bean。并且为这个 Bean 增加 name 属性。

```java
// 创建容器，并且注册Bean
AppContext appContext = Hasor.create().build(apiBinder -> {
    apiBinder.bindType(PojoBean.class)// 注册Bean。
             .dynamicProperty("name", String.class); // 增加名称为 name 类型为 String 的属性。
});

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

执行结果会在控制台上打印出 "data = Hello"
