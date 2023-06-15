---
id: filter
sidebar_position: 1
title: 请求拦截器
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 请求拦截器

在 Hasor 中，一共有三种不同的方式实现请求拦截：
- 通过 `InvokerFilter` 接口拦截请求（推荐）。
- 通过 `javax.servlet.Filter` 接口拦截请求。
- 通过 `Aop` 实现请求拦截器。

其中 `InvokerFilter` 接口和 `Filter` 接口的工作方式和原理是等价的，只是用了不同的接口。
