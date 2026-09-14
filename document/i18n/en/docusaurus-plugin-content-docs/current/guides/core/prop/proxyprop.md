---
id: proxyprop
sidebar_position: 3
title: 2.4.2 Delegated Dynamic Properties
description: Delegate dynamic property reads and writes to an interface.
---

# 2.4.2 Delegated Dynamic Properties

The previous two sections showed what dynamic properties are and how to use them simply. The most powerful part of dynamic properties is delegated property values.

:::tip
Delegating a property value means that when the program calls the property's get/set methods, the method call is mapped to a corresponding interface. Reading and writing the property is implemented entirely by that interface.
:::

```java
public interface PropertyDelegate {
    /** The get method of this delegated property. The parameter is the object that owns the property. */
    public Object get(Object target) throws Throwable;

    /** The set method of this delegated property. The first parameter is the owning object, and the second is the new value. */
    public void set(Object target, Object newValue) throws Throwable;
}
```

A typical use case is sharing the same property between multiple beans:

```java
// Register two beans and share the same name property.
AppContext appContext = Hasor.create().build(apiBinder -> {
    SimplePropertyDelegate delegate = new SimplePropertyDelegate("helloWord");
    apiBinder.bindType(PojoBean1.class).dynamicProperty("name", String.class, delegate);
    apiBinder.bindType(PojoBean2.class).dynamicProperty("name", String.class, delegate);
});

// Create two beans.
PojoBean1 pojoBean1 = appContext.getInstance(PojoBean1.class);
PojoBean2 pojoBean2 = appContext.getInstance(PojoBean2.class);
//
assert BeanUtils.readProperty(pojoBean1, "name").equals("helloWord");
assert BeanUtils.readProperty(pojoBean2, "name").equals("helloWord");
BeanUtils.writeProperty(pojoBean1, "name", "newValue");
assert BeanUtils.readProperty(pojoBean1, "name").equals("newValue");
assert BeanUtils.readProperty(pojoBean2, "name").equals("newValue");
```
