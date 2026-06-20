---
id: bean-proxy-spring
sidebar_position: 4
title: Delegating Bean Management to Spring
description: Delegate Hasor bean creation to Spring.
---
# Delegating Bean Management to Spring

When Hasor meets Spring, bean management is usually the first concern. Hasor and Spring each have their own bean container, while an application architecture usually wants only one primary bean container.

Spring is often the main development framework, and some Hasor features, such as Dataway, are used as complementary capabilities. In that situation, bean management should be delegated to Spring.

Delegating a bean to Spring is done through `TypeSupplier`, which delegates the creation process.

```java
public class MyModule implements WebModule, SpringModule {
    public void loadModule(WebApiBinder apiBinder) {
        final TypeSupplier springTypeSupplier = springTypeSupplier(apiBinder);

        // Register a bean in Hasor, while the real object is created by Spring.
        apiBinder.bindType(Hello.class).toProvider(() -> springTypeSupplier.get(Hello.class));

        // Register an SPI and delegate its creation to Spring.
        apiBinder.bindSpiListener(HelloSpi.class, () -> springTypeSupplier.get(HelloSpi.class));

        // Load a Module configured in Spring.
        apiBinder.loadModule(InSpringModule.class, springTypeSupplier);

        // Load a web controller and delegate controller creation to Spring.
        apiBinder.loadMappingTo(Hello.class, springTypeSupplier);
    }
}
```

In summary, delegated creation mainly relies on two patterns:
- Pattern 1: use `Supplier` for lazy bean creation, and create the bean through Spring `TypeSupplier` when it is needed. You can also inject the Spring context directly and obtain the bean from Spring.
- Pattern 2: use the overloads provided by `ApiBinder` that accept a `TypeSupplier` parameter.

For example, `InSpringModule` in the previous example is a module located in Spring.

```java
@Component
public class ExampleModule implements Module {
    @Autowired
    private DataSource dataSource = null; // Injected by Spring.

    public void loadModule(WebApiBinder apiBinder) {
        ....
    }
}
```
