---
id: interceptor
sidebar_position: 4
title: 4.6.3 MVC 拦截器
description: 在 WebMvcConfigurer 中按注册顺序配置 HandlerInterceptor。
---

# 4.6.3 MVC 拦截器

本节功能自 **5.3.0** 起提供。

`net.hasor.web.HandlerInterceptor` 只作用于已匹配 Action 的 MVC 请求。
HTTP 过滤器位于它的外层，静态资源和继续交给 Servlet 的请求不经过 MVC 拦截器。

## 注册与顺序

```java
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(WebApiBinder binder) {
        binder.bindInterceptor(new LoginInterceptor());
        binder.bindInterceptor(new AuditInterceptor());
    }
}
```

`WebApiBinder.bindInterceptor(HandlerInterceptor interceptor)` 按调用注册的先后顺序执行前置处理。
用户在 `WebMvcConfigurer` 实现中安排注册顺序，框架直接使用这一顺序。
上例的 Login 在 Audit 之前进入；后置处理和完成回调逆序执行。
拦截器实例会被多个请求共享，请求状态应保存到 `Invoker` 或请求属性中。
在模块配置期间完成注册，Web 上下文初始化时取得拦截器列表，请求执行时直接复用，无需逐次从容器查询。

使用 `hasor-config` 时，可在 `WebMvcConfigurer.addInterceptors(WebApiBinder)` 中使用同一注册 API。

## 生命周期

```java
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(Invoker invoker) {
        if (invoker.getHttpRequest().getUserPrincipal() == null) {
            invoker.getHttpResponse().setStatus(401);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(Invoker invoker, Object result) {
        // Inspect or replace Invoker.RETURN_DATA_KEY before rendering.
    }

    @Override
    public void afterCompletion(Invoker invoker, Throwable failure) {
        // Release resources acquired by this interceptor.
    }
}
```

- `preHandle`：在 Controller 之前按序执行。返回 `false` 表示请求已处理，停止后续拦截器、Controller 和自动渲染。
- `postHandle`：Controller 正常返回后逆序执行，之后才进行渲染。可通过 `invoker.put(Invoker.RETURN_DATA_KEY, result)` 修改最终结果。
- `afterCompletion`：渲染结束或请求中断后逆序执行，仅通知 `preHandle` 已返回 `true` 的拦截器。返回 `false` 或前置处理抛出异常的当前拦截器不参与该回调。

正常请求的执行顺序是：外层 Filter → 前置处理 → Controller → 逆序后置处理 → 渲染 → 逆序完成回调 → 返回外层 Filter。
`preHandle`、Controller 或 `postHandle` 失败时进入 [MVC 异常处理](../response/exception.md)。渲染在调用及异常处理结束后执行，渲染失败直接向外抛出，并传递给 `afterCompletion`。
调用正常结束或异常已解决，且渲染成功或被跳过时，完成回调的 `failure` 为 `null`；否则传入最终向外抛出的异常，包括渲染异常。不会为异常恢复结果补跑 `postHandle`。
完成回调自身失败只记录日志，其余完成回调继续执行。

## 自行响应与异步

取得响应 Writer 或输出流后，框架不再追加自动渲染内容。
前置拦截器只设置状态码或响应头、不输出正文时，返回 `false` 即可终止 MVC。
`postHandle` 可调用 `invoker.setSkipRender()` 跳过后续自动渲染，包括显式选择的渲染器和视图。
MVC 主链路在输出前完成检查，渲染器内部不判断该状态。
该方法不终止其余拦截器回调；`preHandle` 中需要终止 MVC 时仍应返回 `false`。
通过 `invoker.isSkipRender()` 查询状态。状态仅属于当前请求，一旦开启跳过渲染，就不能恢复渲染。

框架 `@Async` Action 的 MVC 回调在工作线程执行，响应输出限制见[异步请求](../j2ee/async.md)。
Action 自行调用 Servlet `startAsync()` 后，正常返回仍会执行 `postHandle`，但异步状态下不会自动渲染。`afterCompletion` 在当前 MVC 调用结束时执行，不会自动延迟到 Servlet 异步请求完成；异步完成、错误和超时应通过 Servlet `AsyncListener` 处理。
自行创建的异步任务不处于 MVC 异常处理的调用栈中。
