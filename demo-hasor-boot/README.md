# Demo Hasor Boot

This directory contains Hasor Boot demos.

- `demo-hasor-boot-basic`: command-line Hasor Boot demo.
- `demo-hasor-boot-web`: simple Hasor Web MVC demo running on embedded Tomcat.

Build all executable archives:

```bash
../gradlew :demo-hasor-boot-basic:bootJar :demo-hasor-boot-web:bootJar
```

Run the command-line demo:

```bash
java -jar demo-hasor-boot-basic/build/libs/demo-hasor-boot-basic-5.0.1-SNAPSHOT-boot.jar demo
```

Run the Web demo:

```bash
java -jar demo-hasor-boot-web/build/libs/demo-hasor-boot-web-5.0.1-SNAPSHOT-boot.jar
```

The Web demo uses the `hasor.http` defaults from `hasor-web`; for example, set `HASOR_HTTP_PORT=18080` to change the port.
