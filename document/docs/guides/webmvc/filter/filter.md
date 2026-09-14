---
id: filter
sidebar_position: 1
title: 4.6 请求拦截器
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.6 请求拦截器

在 Hasor 中，一共有三种不同的方式实现请求拦截：
- 通过 `InvokerFilter` 接口拦截请求（推荐）。
- 通过 `javax.servlet.Filter` 接口拦截请求。
- 通过 `Aop` 实现请求拦截器。

其中 `InvokerFilter` 接口和 `Filter` 接口的工作方式和原理是等价的，只是用了不同的接口。
