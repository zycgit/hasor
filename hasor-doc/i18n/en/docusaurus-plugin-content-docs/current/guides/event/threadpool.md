---
id: pool
sidebar_position: 6
title: e.事件线程池
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 事件线程池

默认配置下，Hasor 执行事件的线程池大小是 8。可以在配置文件中修改这个设定：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <!-- 执行事件的线程池大小 -->
        <eventThreadPoolSize>16</eventThreadPoolSize>
    </hasor>
</config>
```

也可以把配置值交给启动参数控制：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <eventThreadPoolSize>${HASOR_LOAD_EVENT_POOL:8}</eventThreadPoolSize>
    </hasor>
</config>
```

```bash
java -DHASOR_LOAD_EVENT_POOL=16 -jar app.jar
```
