# Demo Hasor Boot

This directory contains Hasor Boot executable archive demos.

- `demo-hasor-boot-basic`: command-line Hasor Boot demo.
- `demo-hasor-boot-web`: simple Hasor Web MVC demo running on embedded Tomcat.

Build all demo executable archives:

```bash
mvn -pl demo-hasor-boot/demo-hasor-boot-basic,demo-hasor-boot/demo-hasor-boot-web -am package
```

Run the command-line demo:

```bash
java -jar demo-hasor-boot/demo-hasor-boot-basic/target/demo-hasor-boot-basic-5.0.0-SNAPSHOT.jar demo
```

Run the Web demo:

```bash
java -jar demo-hasor-boot/demo-hasor-boot-web/target/demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```

The Web demo uses the `hasor.http` defaults from `hasor-web`; for example, set `HASOR_HTTP_PORT=18080` to change the port.
