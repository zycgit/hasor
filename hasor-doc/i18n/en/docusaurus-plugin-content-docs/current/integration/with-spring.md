---
id: with-spring
sidebar_position: 3
title: Spring XML Integration
description: Integrate Hasor by using Spring XML configuration.
---
# Spring XML Integration

## Importing the `<h:*/>` Tags

In a Spring configuration file, first import the namespace information for the `<h:*/>` tags.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:h="http://www.hasor.net/schema/spring-hasor"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd
        http://www.hasor.net/schema/spring-hasor https://www.hasor.net/schema/spring-hasor/spring-hasor-4.1.2.xsd">
    ...
</beans>
```

## Enabling Hasor

After importing the namespace, enabling Hasor only requires one tag.

```xml
<h:hasor/>
```

## Sharing Spring Configuration

Hasor can read properties from the Spring `Environment` during startup.

The `useProperties` attribute controls whether Spring properties are imported into Hasor `Settings`. The default value is `false`, which means they are not imported.

```xml
<h:hasor useProperties="true"/>
```

```java
Settings settings = appContext.getSettings();

assert "HelloWord".equals(settings.getString("msg")); // No value is available here when useProperties = false.
```

## Configuring the Scan Range

Generally, when a module is annotated with `@DimModule` and is also managed by Spring, no extra scan range is needed. Sometimes, however, you may want to load modules that are marked with `@DimModule` but are not managed by Spring yet. In that case, configure a scan range. Hasor instantiates those modules when loading them.

```xml
<h:hasor>
    <!-- Note: scanPackages is different in Hasor and Spring. Hasor configures a wildcard, not a package name. -->
    <h:loadModule scanPackages="net.hasor.test.spring.mod2.*"/>
</h:hasor>
```

## Specifying the Hasor Configuration File

Sharing Spring configuration solves most configuration-loading needs, but some applications still need the more advanced `hconfig.xml` file. Use this attribute to specify Hasor's `hconfig.xml`.

```xml
<h:hasor>
    <h:mainConfig>classpath:net_hasor_spring/example-hconfig.xml</h:mainConfig>
</h:hasor>
```

## Startup Entry

`startWithRef` and `startWith` declare the startup entry. The difference is that `startWithRef` references another Spring bean, while `startWith` configures a class name.

```xml
<h:hasor startWithRef="testModuleA"/>
```

or

```xml
<h:hasor startWith="net.hasor.test.spring.mod1.TestModuleA"/>
```

:::tip
To keep the attribute meaning clear, only one of `startWithRef` and `startWith` takes effect. `startWithRef` has priority.
:::

## Loading More Modules

`startWith` can load only one module. If you need to load multiple modules, use the following approach.

The advantage of the `h:module` tag is that the module does not need to carry the `@DimModule` annotation.

```xml
<h:hasor>
    <h:loadModule>
        <!-- Reference a Module from the Spring container. -->
        <h:module refBean="testModuleB"/>
        <!-- A class configured here is first defined as a Spring bean and then referenced. -->
        <h:module class="net.hasor.test.spring.mod1.TestModuleC"/>
    </h:loadModule>
</h:hasor>
```

:::tip
`startWithRef` and `startWith` can configure only one module, while `h:loadModule` can configure multiple modules. Other than that, there is no functional difference.
:::

## Passing Properties to Hasor

Besides importing the Spring `Environment` with `useProperties`, you can also pass selected properties to Hasor `Settings`.

There are two import methods:
- Import with the `h:property` tag.
- Import with the `refProperties` attribute.

```xml
<!-- placeholder performs ${env1} text replacement. -->
<context:property-placeholder location="classpath:net_hasor_spring/env-1.properties"/>

<!-- Load a property file and pass it to Hasor later. -->
<util:properties id="customProperties" location="classpath:net_hasor_spring/env-2.properties"/>

<!-- Create the Hasor container. -->
<h:hasor refProperties="customProperties">
    <h:property name="msg_a">${env1}</h:property>
    <h:property name="msg_b" value="ccc"/>
</h:hasor>
```

## Referencing Hasor Services or Beans

The `h:bean` tag can define a bean in Spring while letting Hasor create the actual object.

```xml
<!-- This bean is obtained through AppContext.getInstance('helloWord'). -->
<h:bean id="hasorBean1" refID="helloWord"/>

<!-- This bean is obtained through AppContext.getInstance(HasorBean.class). -->
<h:bean id="hasorBean2" refType="net.hasor.test.spring.HasorBean"/>

<!-- This bean is obtained through AppContext.getInstance("abc", HasorBean.class). -->
<h:bean id="hasorBean3" refName="abc" refType="net.hasor.test.spring.HasorBean"/>
```

## Multiple Hasor Environments

The XML approach can start multiple Hasor instances from one Spring XML configuration file.

```xml
<!-- Create Hasor container 1. -->
<h:hasor id="hasor_1" startWithRef="testModuleA"/>

<!-- Create Hasor container 2. -->
<h:hasor id="hasor_2" startWith="net.hasor.test.spring.mod1.TestModuleD"/>
```

When declaring Hasor beans in multiple environments, specify `hasorID`.

```xml
<h:bean id="aa1" refID="xxxxx" hasorID="hasor_1"/>
<h:bean id="bb2" refID="xxxxx" hasorID="hasor_2"/>
```

Integrate Spring MVC with Hasor Web:

```xml
<!-- Configure the Hasor dispatcher. -->
<h:web-dispatcher>
    <h:mapping path="/*"/>
</h:web-dispatcher>
```

To start Hasor Web in an XML-based Spring environment, also configure the corresponding listener in `web.xml`.

```xml
<!-- Hasor -->
<listener>
    <listener-class>net.hasor.spring.web.SpringRuntimeListener</listener-class>
</listener>
```

Note that `SpringRuntimeListener` must be configured after `org.springframework.web.context.ContextLoaderListener`; otherwise the "spring application context not initialize." exception will be thrown.
