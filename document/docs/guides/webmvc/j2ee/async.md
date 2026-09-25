---
id: async
sidebar_position: 4
title: 4.8.4 异步请求(Servlet3.0)
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.8.4 异步请求(Servlet3.0)

在Servlet 3.0之前，Servlet采用 Thread-Per-Request 的方式处理请求，即每一次Http请求都由某一个线程从头到尾负责处理。
如果一个请求需要进行IO操作，比如访问数据库、调用第三方服务接口等，那么其所对应的线程将同步地等待IO操作完成。而IO操作是非常慢的，所以此时的线程并不能及时地释放回线程池以供后续使用，在并发量越来越大的情况下，这将带来严重的性能问题。

即便是像Spring、Struts 这样的高层框架也脱离不了这样的桎梏，因为他们都是建立在Servlet之上的。为了解决这样的问题，Servlet 3.0 引入了异步处理，然后在Servlet 3.1中又引入了非阻塞IO来进一步增强异步处理的性能。

Hasor 会自动识别容器的 Servlet 版本。因此 Hasor 在自动识别的帮助下可以做到 Servlet 2.x 和 Servlet 3.x 标准互容，这似的 Hasor 可以同时工作在两种 Servlet 平台之上。

如果你想使用 Servlet 3.0 的异步请求，先要确保你的 Web 容器支持 Servlet 3.0，否则异步请求会当做普通请求处理。

然后像如下这样标记一个 `@Async` 就可以了，Hasor 会自动在 Servlet 3.0 容器下通过 `javax.servlet.AsyncContext.start` 方法启动异步处理。

MVC 拦截器、Action 和异常处理器在工作线程执行。当前处于 Servlet 异步状态的请求不会自动渲染返回值，响应需要由工作线程显式写出。
手动配置 `RuntimeFilter` 时，需要启用异步支持并映射 `ASYNC` dispatcher；Hasor Boot 的三个内嵌容器已完成这项配置。
`afterCompletion` 表示当前 MVC 调用结束，不会等待 Servlet 异步请求结束；应用自己的异步任务应使用 `AsyncListener` 处理完成、错误和超时。
当前未处理异常的异步传播尚未完成，不能依赖它自动产生 HTTP 错误响应。异步任务应自行处理异常并写出响应；通过 `ExceptionHandler` 写出响应时仍需返回非 `null` 值表示异常已处理。

```java title='例子'
@Async
@MappingTo("/helloAction.do")
public class HelloAction {
    @Any
    public void execute(@RequestParameter("name") String userName,
                        @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

或者标记在方法上

```java title='配置拦截器'
@MappingTo("/helloAction.do")
public class HelloAction {
    @Async
    @Any
    public void execute(@RequestParameter("name") String userName,
                        @RequestParameter("pwd") String pwd) {
        ...
    }
}
```

## 异步任务的结束流程

从 **5.3.0** 开始，`AsyncInvocationWorker` 将工作执行、错误处理与结束分开：

- `doWork(Method)` 正常结束后，`finish(true)` 调用 `AsyncContext.complete()`。
- 工作抛出异常时，先调用 `doWorkWhenError(Method, Throwable)`；框架的实现将异常记录到调用的 `Future`，随后 `finish(false)` 调用 `AsyncContext.dispatch()`。
- 自定义错误回调只负责处理或记录异常，不应再次调用 `complete()` 或 `dispatch()`；需要改变结束策略时重写 `finish(boolean)`。

这里的 `dispatch()` 只是重新分发请求。当前 `RuntimeFilter` 尚未将原异步失败完整衔接到容器错误处理，不能据此保证返回 HTTP 500 或错误正文；应用仍需按上文的限制显式处理异步响应。
