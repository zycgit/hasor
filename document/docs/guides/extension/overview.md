---
id: overview
sidebar_position: 0
title: 扩展机制
description: 使用 Module、ApiBinder 和 SPI 扩展 Hasor 框架能力。
---

# 扩展机制

Hasor 通过 `Module`、`ApiBinder` 和 SPI 提供扩展能力。应用可以把配置拆分为多个模块，也可以向框架注册自定义扩展点。

本章节包括：

- `Module` 的组织方式。
- `ApiBinder` 提供的绑定和扩展入口。
- SPI 的发现和加载机制。
