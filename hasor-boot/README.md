# Hasor Boot

Hasor Boot is the executable archive layer for Hasor applications.

## Modules

| Module | Responsibility |
| --- | --- |
| `hasor-boot-loader` | Runtime launcher and class loading support for Hasor executable archives. |
| `hasor-boot-maven-plugin` | Maven packaging integration for generating Hasor Boot fat jars. |
| `hasor-boot-gradle-plugin` | Gradle packaging integration for generating Hasor Boot fat jars. |

## Archive Layout

```text
META-INF/MANIFEST.MF
APP-INF/classes/
APP-INF/lib/
APP-INF/hasor/
```

The launcher owns the Hasor Boot manifest contract and delegates nested archive resource access to Cobble loader APIs.
