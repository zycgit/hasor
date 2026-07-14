---
id: data-access
sidebar_position: 3
title: 03. 数据访问
description: 通过 dbVisitor 为 Hasor 项目提供数据库访问能力。
---

# 数据访问

Hasor 当前核心模块保持在 `hasor-core`、`hasor-web`、`hasor-boot`，自身不再内置 ORM 或数据库访问框架。数据库访问能力由同一生态下的 dbVisitor 提供，它负责 SQL 执行、Mapper、事务、分页和多数据库适配等能力。

在 Hasor 项目中使用 dbVisitor 时，Hasor 负责应用容器、依赖注入、启动和 Web 集成，dbVisitor 负责数据访问层。这样可以让 Hasor 核心保持轻量，也让数据访问能力在 dbVisitor 文档中持续维护。

## 常用入口

- [dbVisitor 首页](https://www.dbvisitor.net)
- [dbVisitor 介绍](https://www.dbvisitor.net/docs/guides/overview)
- [在 Hasor 项目中使用 dbVisitor](https://www.dbvisitor.net/docs/guides/yourproject/with_hasor)
- [源码 Gitee](https://gitee.com/zycgit/dbvisitor)
- [源码 GitHub](https://github.com/zycgit/dbvisitor)
