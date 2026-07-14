---
id: data-access
sidebar_position: 3
title: 03. Data Access
description: Use dbVisitor to provide database access capabilities for Hasor projects.
---

# Data Access

Hasor now keeps its core modules focused on `hasor-core`, `hasor-web`, and `hasor-boot`. It no longer embeds an ORM or database access framework. Data access is provided by dbVisitor, a project in the same ecosystem that covers SQL execution, Mapper APIs, transactions, pagination, and multi-database adaptation.

In a Hasor project, Hasor provides the application container, dependency injection, bootstrapping, and Web integration, while dbVisitor provides the data access layer. This keeps Hasor lightweight and lets database access documentation continue to live with dbVisitor.

## Useful Links

- [dbVisitor Home](https://www.dbvisitor.net)
- [dbVisitor Overview](https://www.dbvisitor.net/docs/guides/overview)
- [Use dbVisitor in Hasor projects](https://www.dbvisitor.net/docs/guides/yourproject/with_hasor)
- [Source Code on Gitee](https://gitee.com/zycgit/dbvisitor)
- [Source Code on GitHub](https://github.com/zycgit/dbvisitor)
