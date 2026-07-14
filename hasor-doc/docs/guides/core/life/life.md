---
id: life
sidebar_position: 1
title: 生命周期
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 生命周期

Hasor 的生命周期大致分为三个阶段：init、start、shutdown。

![](../../_img/CC2_040A_4409_E756.png)

Init 阶段
- findModules：在配置文件中，查找找所有可以加载的 Module。
- doInitialize：执行 init 阶段的起始标志，默认是空实现。
- newApiBinder：创建 Module 在执行 loadModule 方法时用到的 ApiBinder 对象，包括 ApiBinder 的扩展机制也是在这里给予支持。
- installModule ：加载每一个 Module，简单来说就是一个 for。
- doBind：容器级的初始化操作，这个过程细分为 doBindBefore、installModule、doBindAfter 三个部分。
- doInitializeCompleted：执行 init 阶段的终止标志，默认是空实现。

Start 阶段
- doStart：执行 start 阶段的起始标志。
- fireSyncEvent ：通过事件机制发送 ContextEvent_Started 事件。
- doStartCompleted ：执行 start 阶段的终止标志。

Shutdown 阶段
- doShutdown：执行 shutdown 阶段的起始标志。
- fireSyncEvent：发送 ContextEvent_Shutdown 事件。
- doShutdownCompleted：执行 shutdown 阶段的终止标志。