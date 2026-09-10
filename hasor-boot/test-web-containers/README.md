# Web 容器共享测试

为 Tomcat、Jetty、Undertow 提供同一套 HTTP 集成测试，验证自动扫描、响应渲染、静态资源、路由冲突及配置覆盖等行为。

采用标准 Java 模块结构：`src/main/java` 存放供容器复用的测试基类与测试应用，`src/main/resources` 存放测试资源。后续本模块自身的单元测试放在 `src/test/java`，对应资源放在 `src/test/resources`。

各容器模块通过普通测试依赖引用本模块的 JAR，并以具体的 JUnit 测试类继承执行：

```groovy
testImplementation project(':test-web-containers')
```

在仓库根目录执行：

```bash
./gradlew :hasor-boot-web-tomcat:test :hasor-boot-web-jetty:test :hasor-boot-web-undertow:test
```

本模块不依赖具体容器、不参与发布，也不进入容器的生产运行依赖。单独执行本模块的 `test` 不会启动容器或执行这些共享用例。

测试基类显式提供包含测试应用 JAR 的 URLClassLoader，测试完成后恢复并关闭。当前扫描器无法从 Java 17 默认 AppClassLoader 枚举普通依赖 JAR，因此这里统一测试类加载环境；这些测试不代表该默认类加载器下的 JAR 扫描兼容性已修复。
