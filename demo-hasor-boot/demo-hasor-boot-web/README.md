# Demo Hasor Boot Web

Build the executable archive:

```bash
../../gradlew :demo-hasor-boot-web:bootJar
```

Run it:

```bash
java -jar build/libs/demo-hasor-boot-web-5.0.1-SNAPSHOT-boot.jar
```

The HTTP server is started by `WebServers.run(...)`. Its defaults come from the `hasor.http` section in `hasor-web` and can be overridden with environment variables such as `HASOR_HTTP_PORT`.

Then open:

```text
http://localhost:8080/hello
```
