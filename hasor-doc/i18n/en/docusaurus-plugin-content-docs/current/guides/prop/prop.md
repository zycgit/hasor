---
id: prop
sidebar_position: 1
title: Dynamic Properties
description: Add properties to beans dynamically through proxy capabilities.
---

# Dynamic Properties

:::tip
- This feature was added in version 4.2.1.
- A "dynamic property" means that after a bean type is defined, a property that did not originally exist can be dynamically added to it through dynamic proxy capability.
- The property is attached to the object like an invisible companion. It follows the bean for its lifetime, while users of the bean do not need to be aware of it.
:::

In some difficult code paths, transparent pass-through is a useful way to solve problems. Dynamic properties let you pass additional information through different layers without modifying the original type.
