---
id: pool
sidebar_position: 6
title: e.事件线程池
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 事件线程池

默认配置下，Hasor 执行事件的线程池是 8 您可以通过下面两种方式修改这个设定：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor.environmentVar>
        <!-- 执行事件的线程池大小 -->
        <HASOR_LOAD_EVENT_POOL>8</HASOR_LOAD_EVENT_POOL>
    </hasor.environmentVar>
</config>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <!-- 执行事件的线程池大小 -->
    <hasor.eventThreadPoolSize>8</hasor.eventThreadPoolSize>
</config>
```