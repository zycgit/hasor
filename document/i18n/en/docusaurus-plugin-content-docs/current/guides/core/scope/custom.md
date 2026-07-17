---
id: custom
sidebar_position: 4
title: c. Custom Scopes
description: Implement and register a custom Hasor scope.
---

# Custom Scopes

Use `HttpSession` as an example to implement an `HttpSession` scope.

```java
public class SessionScope implements Scope {
    public static final ThreadLocal<HttpSession> session
            = new ThreadLocal<HttpSession>();

    public <T> Supplier<T> scope(Object key, Supplier<T> provider) {
        HttpSession httpSession = session.get();
        if (httpSession == null) {
            return provider;
        }
        // To avoid conflicts between beans stored in Session and keys already in the Session,
        // add a prefix to distinguish them.
        String keyStr = "session_scope_" + key.toString();
        Object attribute = httpSession.getAttribute(keyStr);
        Supplier<T> finalProvider = provider;
        if (attribute == null) {
            httpSession.setAttribute(keyStr, provider);
        } else {
            finalProvider = (Supplier<T>) httpSession.getAttribute(keyStr);
        }
        return finalProvider;
    }
}
```

Then use a `Filter` to set the `Session` object into `ThreadLocal` whenever a `request` arrives.

```java
public class ConfigSession implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            if (SessionScope.session.get() != null) {
                SessionScope.session.remove();
            }
            SessionScope.session.set(((HttpServletRequest) request).getSession(true));
            chain.doFilter(request, response);
        } finally {
            if (SessionScope.session.get() != null) {
                SessionScope.session.remove();
            }
        }
    }
}
```

Finally, configure the scope when creating Hasor. Because a filter needs to be configured here, use `WebModule`.

```java
public class StartModule implements WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.filter("/*").through(0, new ConfigSession());
        apiBinder.bindScope("session", new SessionScope());
        ...
    }
}
```

Next, configure each `UserInfo` object to be unique within the session:

```java
apiBinder.bindType(UserInfo.class).toScope("session");
```
