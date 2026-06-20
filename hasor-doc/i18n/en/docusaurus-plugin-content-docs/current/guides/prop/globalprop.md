---
id: globalprop
sidebar_position: 4
title: c. Global Dynamic Properties
description: Configure dynamic properties globally for matching beans.
---

# Global Dynamic Properties

Setting dynamic properties separately for each bean can be troublesome, so dynamic properties can also be configured globally. The usage is somewhat similar to configuring AOP.

```java
AppContext appContext = Hasor.create().build(apiBinder -> {
apiBinder.dynamicProperty(
        t -> true,  // Matching class, type: Predicate<Class<?>>.
        "name",     // Property name.
        String.class // Property type.
    );
});
```

Then, after creating a bean, you can call the corresponding get/set methods through reflection.

```java
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
