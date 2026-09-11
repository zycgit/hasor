# Demo Hasor Boot

This directory contains Hasor Boot demos split by build tool.

- `gradle/demo-hasor-boot-basic`: command-line Hasor Boot demo built with Gradle.
- `gradle/demo-hasor-boot-web`: Hasor Web MVC demo built with Gradle.
- `maven/demo-hasor-boot-basic`: command-line Hasor Boot demo built with Maven.
- `maven/demo-hasor-boot-web`: Hasor Web MVC demo built with Maven.

Build Gradle demos:

```bash
cd gradle
../../gradlew bootJar
```

Build Maven demos:

```bash
cd maven
mvn package
```

Run Gradle basic demo:

```bash
java -jar gradle/demo-hasor-boot-basic/build/libs/*-boot.jar demo
```

Run Gradle web demo:

```bash
java -jar gradle/demo-hasor-boot-web/build/libs/*-boot.jar
```

Run Maven basic demo:

```bash
java -jar maven/demo-hasor-boot-basic/target/*-boot.jar demo
```

Run Maven web demo:

```bash
java -jar maven/demo-hasor-boot-web/target/*-boot.jar
```

The Web demo uses the `hasor.boot.web` defaults from `hasor-boot-web`; for example, set `HASOR_HTTP_PORT=18080` to change the port.
