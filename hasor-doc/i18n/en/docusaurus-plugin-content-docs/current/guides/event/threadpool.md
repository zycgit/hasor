---
id: pool
sidebar_position: 6
title: e. Event Thread Pool
description: Configure the Hasor event execution thread pool.
---

# Event Thread Pool

By default, Hasor uses a thread pool size of 8 for event execution. This setting can be changed in the configuration file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config xmlns="http://www.hasor.net/sechma/main">
    <hasor>
        <!-- Thread pool size for event execution. -->
        <eventThreadPoolSize>16</eventThreadPoolSize>
    </hasor>
</config>
```

You can also let startup parameters control the configuration value:

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
