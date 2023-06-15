---
id: custom
sidebar_position: 4
title: c.自定义作用域
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 自定义作用域

以 HttpSession 为例，实现一个 `HttpSession` 作用域。

```java
public class SessionScope implements Scope {
    public static final ThreadLocal<HttpSession> session
            = new ThreadLocal<HttpSession>();

    public <T> Provider<T> scope(Object key, Provider<T> provider) {
        HttpSession httpSession = session.get();
        if (httpSession == null) {
            return provider;
        }
        // 为了避免保存到 Session 中的 Bean 和本身 Session 中的数据 key
        // 出现冲突，增加一个前缀用于区分
        String keyStr = "session_scope_" + key.toString();
        Object attribute = httpSession.getAttribute(keyStr);
        Provider<T> finalProvider = provider;
        if (attribute == null) {
            httpSession.setAttribute(keyStr, provider);
        } else {
            finalProvider = (Provider<T>) httpSession.getAttribute(keyStr);
        }
        return finalProvider;
    }
}
```

然后通过一个 `Filter` 每次 `request` 请求到来的时候把 `Session` 对象设置到 `ThreadLocal` 中。

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

最后我们在创建 Hasor 的时候把 Scope 配置上，这里由于要配置 Filter 因此使用 WebModule

```java
public class StartModule extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        ...
        apiBinder.filter("/*").through(0, new ConfigSession());
        apiBinder.registerScope("session", new SessionScope());
        ...
    }
}
```

接下来配置每次创建 UserInfo 对象时都是 Session 内唯一：

```java
apiBinder.bindType(UserInfo.class).toScope("session");
```
