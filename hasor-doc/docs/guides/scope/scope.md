---
id: scope
sidebar_position: 1
title: 作用域(Scope)
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 作用域(Scope)

Hasor 在管理 Bean 的时候支持作用域，一个典型的作用域应用场景就是“单例”。单例作用域的表现是整个应用程序中只保存一份。

另外一个作用域的例子是用户登录网站之后 web 应用程序通过 Session 保持会话。
