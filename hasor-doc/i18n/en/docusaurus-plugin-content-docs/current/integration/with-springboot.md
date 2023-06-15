---
id: with-springboot
sidebar_position: 2
title: Spring Boot 整合
description: dbVisitor ORM 工具和 Spring Boot 整合使用。
---
# 与 SpringBoot 集成

Dataway 是 Hasor 生态中的一员，使用 Dataway 第一步需要通过 hasor-spring 打通两个生态。

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-spring</artifactId>
    <version>4.2.5</version>
</dependency>
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>hasor-dataway</artifactId>
    <version>4.2.5</version>
</dependency>
```

启用 Hasor

```js
@EnableHasor()    // 在Spring 中启用 Hasor
@EnableHasorWeb() // 将 hasor-web 配置到 Spring 环境中，Dataway 的 UI 是通过 hasor-web 提供服务。
```

启用 Dataway

然后第二步，在应用的 application.properties 配置文件中启用 Dataway

```properties
# 启用 Dataway 功能（默认不启用）
HASOR_DATAQL_DATAWAY=true
# 开启 ui 管理功能（注意生产环境必须要设置为 false，否则会造成严重的生产安全事故）
HASOR_DATAQL_DATAWAY_ADMIN=true

# （可选）API工作路径
HASOR_DATAQL_DATAWAY_API_URL=/api/
# （可选）ui 的工作路径，只有开启 ui 管理功能后才有效
HASOR_DATAQL_DATAWAY_UI_URL=/interface-ui/
```

初始化必要的表(例：MySQL)

```sql
CREATE TABLE interface_info (
  api_id          varchar(64)  NOT NULL COMMENT 'ID',
  api_method      varchar(12)  NOT NULL COMMENT 'HttpMethod：GET、PUT、POST',
  api_path        varchar(512) NOT NULL COMMENT '拦截路径',
  api_status      varchar(4)   NOT NULL COMMENT '状态：-1-删除, 0-草稿，1-发布，2-有变更，3-禁用',
  api_comment     varchar(255) NOT NULL COMMENT '注释',
  api_type        varchar(24)  NOT NULL COMMENT '脚本类型：SQL、DataQL',
  api_script      mediumtext   NOT NULL COMMENT '查询脚本：xxxxxxx',
  api_schema      mediumtext   NOT NULL COMMENT '接口的请求/响应数据结构',
  api_sample      mediumtext   NOT NULL COMMENT '请求/响应/请求头样本数据',
  api_option      mediumtext   NOT NULL COMMENT '扩展配置信息',
  api_create_time varchar(32)  NOT NULL COMMENT '创建时间',
  api_gmt_time    varchar(32)  NOT NULL COMMENT '修改时间',
  PRIMARY KEY (api_id),
  UNIQUE KEY uk_interface_info (api_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dataway 中的API';

CREATE TABLE interface_release (
  pub_id           varchar(64)  NOT NULL COMMENT 'Publish ID',
  pub_api_id       varchar(64)  NOT NULL COMMENT '所属API ID',
  pub_method       varchar(12)  NOT NULL COMMENT 'HttpMethod：GET、PUT、POST',
  pub_path         varchar(512) NOT NULL COMMENT '拦截路径',
  pub_status       varchar(4)   NOT NULL COMMENT '状态：-1-删除, 0-草稿，1-发布，2-有变更，3-禁用',
  pub_comment      varchar(255) NOT NULL COMMENT '注释',
  pub_type         varchar(24)  NOT NULL COMMENT '脚本类型：SQL、DataQL',
  pub_script       mediumtext   NOT NULL COMMENT '查询脚本：xxxxxxx',
  pub_script_ori   mediumtext   NOT NULL COMMENT '原始查询脚本，仅当类型为SQL时不同',
  pub_schema       mediumtext   NOT NULL COMMENT '接口的请求/响应数据结构',
  pub_sample       mediumtext   NOT NULL COMMENT '请求/响应/请求头样本数据',
  pub_option       mediumtext   NOT NULL COMMENT '扩展配置信息',
  pub_release_time varchar(32)  NOT NULL COMMENT '发布时间（下线不更新）',
  PRIMARY KEY (pub_id),
  KEY idx_interface_release_api  (pub_api_id),
  KEY idx_interface_release_path (pub_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dataway API 发布历史。'
```

初始化数据源

```js
@DimModule
@Component
public class ExampleModule implements SpringModule {
    @Autowired
    private DataSource dataSource = null;

    public void loadModule(ApiBinder apiBinder) throws Throwable {
        // .DataSource form Spring boot into Hasor
        apiBinder.installModule(new JdbcModule(Level.Full, this.dataSource));
        // .custom DataQL
        //apiBinder.tryCast(QueryApiBinder.class).loadUdfSource(apiBinder.findClass(DimUdfSource.class));
        //apiBinder.tryCast(QueryApiBinder.class).bindFragment("sql", SqlFragment.class);
    }
}
```

最后一步，将 Spring 使用的数据源导入到 Hasor 环境共 Dataway 使用。

启动工程

在启动日志中看到下列信息输出就表示 Dataway 已经可以正常访问了。

```log
2020-04-01 09:13:18.502 [main] INFO  n.h.core.context.TemplateAppContext - loadModule class net.hasor.dataway.config.DatawayModule
2020-04-01 09:13:18.502 [main] INFO  n.hasor.dataway.config.DatawayModule - dataway api workAt /api/
2020-04-01 09:13:18.502 [main] INFO  n.h.c.e.AbstractEnvironment - var -> HASOR_DATAQL_DATAWAY_API_URL = /api/.
2020-04-01 09:13:18.515 [main] INFO  n.hasor.dataway.config.DatawayModule - dataway admin workAt /interface-ui/
```

- `dataway api workAt /api/` 表示 API 的工作路径。
- `dataway admin workAt /interface-ui/` 表示 管理配置界面的地址。

此时访问：`http://<yourIP>:<yourProt>/interface-ui/` 就可以看到配置页面了。

