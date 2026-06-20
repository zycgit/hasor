---
id: valueprop
sidebar_position: 2
title: a. Value-Based Dynamic Properties
description: Add simple get/set dynamic properties to beans.
---

# Value-Based Dynamic Properties

:::tip
Value-based means the attached dynamic property only supports simple get/set behavior. It behaves as if the type simply had one more private field.
:::

In some difficult code paths, passing through additional properties is a useful approach. Dynamic properties let you pass additional information without modifying the original type. For example:

```java
// Original class.
public class PojoBean {
    private String name;
    private int    age;

    // get/set for name.
    public String getName() { ... }
    public void setName(String name) { ... }

    // get/set for age.
    public int getAge() { ... }
    public void setAge(int age) { ... }
}

// Add a property named type without changing the code, as if the class looked like this.
public class PojoBean {
    private String name;
    private int    age;
    private int    type;

    // get/set for name.
    public String getName() { ... }
    public void setName(String name) { ... }

    // get/set for age.
    public int getAge() { ... }
    public void setAge(int age) { ... }

    // get/set for type.
    public int getType() { ... }
    public void setType(int type) { ... }
}
```

First, define a simple bean.

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

Then create the Hasor container, register this bean, and add a `name` property to it.

```java
// Create the container and register the bean.
AppContext appContext = Hasor.create().build(apiBinder -> {
    apiBinder.bindType(PojoBean.class) // Register the bean.
             .dynamicProperty("name", String.class); // Add a property named name with type String.
});

// Create the bean.
PojoBean pojoBean = appContext.getInstance(PojoBean.class);

// Obtain get/set methods.
Method getMethod = pojoBean.getClass().getMethod("getName");
Method setMethod = pojoBean.getClass().getMethod("setName", String.class);

// Inject the name property through reflection.
setMethod.invoke(pojoBean, "Hello");

// Read the name property through reflection.
System.out.println("data = " + getMethod.invoke(pojoBean));
```

The console prints `data = Hello`.
