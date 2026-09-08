---
id: data-access
sidebar_position: 3
title: 03. Data Access
description: Use dbVisitor to provide database access capabilities for Hasor projects.
---

# Data Access

Hasor provides container, Java configuration, Web, and packaging capabilities through `hasor-core`, `hasor-config`, `hasor-web`, and the `hasor-boot` modules. It does not embed an ORM or database access framework. Data access is provided by dbVisitor, a project in the same ecosystem that covers SQL execution, Mapper APIs, transactions, pagination, and multi-database adaptation.

In a Hasor project, Hasor provides the application container, dependency injection, bootstrapping, and Web integration, while dbVisitor provides the data access layer. This keeps Hasor lightweight and lets database access documentation continue to live with dbVisitor.

## Useful Links

- [dbVisitor Home](https://www.dbvisitor.net)
- [dbVisitor Overview](https://www.dbvisitor.net/docs/guides/overview)
- [Use dbVisitor in Hasor projects](https://www.dbvisitor.net/docs/guides/yourproject/with_hasor)
- [Source Code on Gitee](https://gitee.com/zycgit/dbvisitor)
- [Source Code on GitHub](https://github.com/zycgit/dbvisitor)
