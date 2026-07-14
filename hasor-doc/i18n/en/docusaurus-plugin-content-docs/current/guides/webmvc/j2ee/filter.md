---
id: filter
sidebar_position: 2
title: b. Filter
description: Register and use Filters in Hasor Web.
---

# Filter

Use a filter as shown below:

```java title='Example'
public class MyFilter implements Filter {
    ...
}
```

Then register the filter:

```java title='Configure the interceptor'
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.jeeFilter("/*").through(MyFilter.class);
        ...
    }
}
```
