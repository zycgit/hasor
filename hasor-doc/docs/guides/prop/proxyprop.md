---
id: proxyprop
sidebar_position: 3
title: b.委托型动态属性
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 委托型动态属性

前两两个小结中演示了什么叫动态属性以及它的简单用法，动态属性的精华部分是属性值的委托。

:::tip
所谓属性值的委托是指。当程序调用属性的 get/set 方法时，会将方法的调用映射到一个对应的接口。属性的读写完全由接口自己来实现：
:::

```java
public interface PropertyDelegate {
    /** 该委托属性的get方法，参数是属性所处的对象 */
    public Object get(Object target) throws Throwable;

    /** 该委托属性的set方法，第一个参数是属性所处的对象，第二个参数代表设置的新值 */
    public void set(Object target, Object newValue) throws Throwable;
}
```

一个典型的应用场景是，在多个 Bean 之间共享同一个属性：

```java
// 注册两个 Bean 并且共享同一个 name 属性。
AppContext appContext = Hasor.create().build(apiBinder -> {
    SimplePropertyDelegate delegate = new SimplePropertyDelegate("helloWord");
    apiBinder.bindType(PojoBean1.class).dynamicProperty("name", String.class, delegate);
    apiBinder.bindType(PojoBean2.class).dynamicProperty("name", String.class, delegate);
});

// 创建两个 Bean
PojoBean1 pojoBean1 = appContext.getInstance(PojoBean1.class);
PojoBean2 pojoBean2 = appContext.getInstance(PojoBean2.class);
//
assert BeanUtils.readProperty(pojoBean1, "name").equals("helloWord");
assert BeanUtils.readProperty(pojoBean2, "name").equals("helloWord");
BeanUtils.writeProperty(pojoBean1, "name", "newValue");
assert BeanUtils.readProperty(pojoBean1, "name").equals("newValue");
assert BeanUtils.readProperty(pojoBean2, "name").equals("newValue");
```
