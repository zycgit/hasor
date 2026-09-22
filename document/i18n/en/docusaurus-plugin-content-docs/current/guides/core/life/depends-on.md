---
title: Explicit initialization dependencies
---

# DependsOn

`dependsOn(...)` obtains declared dependencies before creating the current bean. Their synchronous initialization completes first. Resolution happens when the bean is created, so later modules may register dependencies.

```java
binder.bindType(Storage.class).idWith("storage");
binder.bindType(Service.class).dependsOn("storage");
```

| Form | Resolution |
| --- | --- |
| `dependsOn("storage")` | Exact binding ID |
| `dependsOn(Storage.class)` | Exactly one registered binding of this type |
| `dependsOn("primary", Storage.class)` | Registered name and type |
| `dependsOn(storageBinding)` | ID of an existing `BindInfo` |

Calls accumulate dependencies. ID, type and binding-reference forms accept multiple arguments. Missing and ambiguous dependencies fail rather than selecting by registration order.

`net.hasor.core.DependsOn` supports inherited type annotations and configuration factory methods:

```java
@DependsOn(value = "storage", types = AuditService.class)
public class Service {
}

@Configuration
public class ApplicationConfiguration {
    @Bean(value = "storage", initMethod = "init")
    public Storage storage() {
        return new Storage();
    }

    @Bean
    @DependsOn("storage")
    public Service service() {
        return new Service();
    }
}
```

`hasor-config` translates method annotations into core dependency declarations. An unnamed `@Bean` uses its method name as the binding ID; an explicit value supplies both ID and name.

Dependencies are resolved before a regular constructor or a custom Provider is invoked. Failure prevents creation of the dependent object. This does not inject or initialize objects returned by arbitrary Providers; their lifecycle remains provider-owned. Configuration methods retain their `@Bean(initMethod = ...)` lifecycle.

Singleton dependencies are obtained on first creation; prototype creation obtains dependencies each time. This does not eagerly instantiate all beans. A prototype dependency may produce another instance on a later lookup; the declaration does not inject that instance.

Explicit dependencies share the core creation-chain cycle detection with constructor and field injection, including diagnostic paths. There is no global topological initialization queue, asynchronous readiness wait or new reverse-destruction ordering.

`binder.getProvider(name, type)` uses the same named/type selection, with blank name requiring one registered candidate. Suppliers become usable after container initialization notifications; the existing type and binding-reference Provider forms now acquire their context at that stage as well.
