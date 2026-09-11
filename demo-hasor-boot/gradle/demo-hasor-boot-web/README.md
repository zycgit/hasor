# Demo Hasor Boot Web Gradle

Build the executable archive:

```bash
../../../gradlew bootJar
```

Run it:

```bash
java -jar build/libs/*-boot.jar
```

The HTTP server is started by `WebServers.run(...)`. Its defaults come from the `hasor.boot.web` section in `hasor-boot-web` and can be overridden with environment variables such as `HASOR_HTTP_PORT`.

Then open:

```text
http://localhost:8080/hello
```
