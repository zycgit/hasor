---
id: overview
sidebar_position: 0
title: Bean Container
description: Hasor Core provides dependency injection, bean management, scopes, dynamic properties, AOP, lifecycle, events, and configuration.
---

# Bean Container

`hasor-core` is the foundation of Hasor. It provides the application container, dependency injection, bean management, scopes, dynamic proxy, lifecycle callbacks, events, and configuration loading.

This chapter is organized by capability:

- Dependency injection: declare object dependencies and let the container create and wire them.
- Bean management: manage multiple instances of the same type, unique IDs, and delegated creation.
- Scopes: control bean creation and reuse.
- Dynamic properties: provide runtime-changeable configuration values.
- Dynamic proxy: add interception logic around method calls.
- Lifecycle: run logic during container startup, initialization, and shutdown.
- Event model: publish and subscribe to events across modules.
- Configuration files: read XML, YAML, Properties, and external environment values.
