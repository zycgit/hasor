---
id: env
sidebar_position: 1
title: 环境变量
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 环境变量

:::tip
- Hasor 对于环境变量的定义是不区分大小写的。建议：环境变量名，要求全部必须大写。
- 通常 Hasor 的环境变量机制还会被用来充当应用程序的配置文件机制，因为它比较简单易用。
:::

声明环境变量有若干种方式，如下：

| 示例                                          | 说明                | 覆盖顺序（靠后优先） |
|---------------------------------------------|-------------------|------------|
| System.getProperties()                      | Java 的系统属性        | 1st        |
| System.getenv()                             | 操作系统环境变量          | 2st        |
| hconfig.xml 配置文件中 hasor.environmentVar 的子节点 | Xml文件中特定位置        | 3st        |
| Hasor.create().addVariable(...) 代码方式        | 在初始化Hasor时通过代码来设置 | 4st        |

在 Hasor 中，上述表格中的数据都会作为环境变量纳入到 Environment 接口中。一旦出现冲突将会按照，表格中覆盖顺序进行覆盖。
