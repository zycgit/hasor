# JDBC 框架

&emsp;&emsp;Hasor-DB 是一款基于 jdbc 的数据库访问框架，最佳实践集中化实现。

----------
## 场景
01. 完全基于 SQL 的数据库读写操作。
02. 单表 Lambda 编码式的数据库读写操作。
03. 基于代码的动态 SQL 生成。
04. 基于 Xml 的动态 SQL 查询。
05. Result 结果集二次处理和转换。
05. 事务下的数据库操作。
05. 分页 + 方言。
06. 数据库元信息检索。
07. ORM
08. 动态 SQL 写在接口上
09. Mybatis 平滑迁移

----------
## 特性
01. 全面支持 JDBC 4.2 各种数据类型
02. 全面支持 Java8 中的各种时间类型
03. 注解化 ORM 能力
04. 七种事务传播行为的控制（同 Spring 一样）
05. 支持 MybatisPlus 风格的 Lambda SQL 生成器
06. JDBC 操作接口 90% 以上兼容 SpringJDBC 写法
07. 多种事务控制方式包括
08. 手动事务
09. 注解式声明
10. TransactionTemplate 模板事务
11. 支持多数据源（不支持分布式事务）
12. 可完全独立于 Hasor 之外单独使用

## 样例

```java
JdbcTemplate jdbcTemplate = new JdbcTemplate();
```

## 源码说明

- 单一数据库无法满足 hasor-db 的单测试要求。因此建议同时启动下列在开发环境中使用了4 个主流数据库
- 例：MySQL 8 驱动层面不支持 JDBC 时区类型，因此采用 Oracle 替代测试。
- 
- docker-compose.yml（MySQL、PG、Oracle、MSSQL）
