# Demo Hasor Boot Web

Build the executable archive:

```bash
mvn -pl demo-hasor-boot/demo-hasor-boot-web -am package
```

Run it:

```bash
java -jar demo-hasor-boot/demo-hasor-boot-web/target/demo-hasor-boot-web-5.0.0-SNAPSHOT.jar
```

The HTTP server is started by `WebServers.run(...)`. Its defaults come from the `hasor.http` section in `hasor-web` and can be overridden with environment variables such as `HASOR_HTTP_PORT`.

Then open:

```text
http://localhost:8080/hello
```
