---
id: scope
sidebar_position: 1
title: 作用域(Scope)
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 作用域(Scope)

Hasor 在管理 Bean 的时候支持作用域，一个典型的作用域应用场景就是“单例”。单例作用域的表现是整个应用程序中只保存一份。

另外一个作用域的例子是用户登录网站之后 web 应用程序通过 Session 保持会话。
