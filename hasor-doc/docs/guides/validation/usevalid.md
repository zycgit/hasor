---
id: usevalid
sidebar_position: 1
title: a.使用验证器
description: DataQL 开发手册，QIL 指令集、构造指令、存储指令、结束指令、运算指令、控制指令、函数指令、辅助指令
---

# 使用验证器

一个请求在递交到后台之后正式处理之前会做一些参数合法性校验。比如：年龄大于1，性别必须是：男或女，帐号密码输入不能为空等。

最后还要把验证的信息反馈到页面上，Hasor 的验证器可以帮助实现这些功能。

以登录场景为例，首先定义请求参数组：

```java
@ValidBy(LoginFormValidation.class)
public class LoginForm {
    @RequestParameter("account")
    private String account;
    @RequestParameter("password")
    private String password;
    ...
}
```

编写验证器

```java
public class LoginFormValidation implements Validation<LoginForm> {
    public void doValidation(String validType,
            LoginForm dataForm,
            ValidInvoker errors) {
        if (StringUtils.isBlank(dataForm.getLogin())) {
            errors.addError("login", "帐号不能为空！");
            return;
        }
        if (StringUtils.isBlank(dataForm.getPassword())) {
            errors.addError("password", "密码不能为空！");
            return;
        }
    }
}
```

最后通过 `@Valid` 注解配置请求在接收处理之前先做一次验证：

```java
@MappingTo("/login.htm")
public class Login {
    public void execute(@Valid() @ParameterGroup LoginForm loginForm,
                        RenderInvoker invoker,
                        ValidInvoker valid) {
        if (valid.isValid()) {
            invoker.renderTo("/userInfo.htm");
        } else {
            invoker.put("loginForm", loginForm);
            invoker.renderTo("/login.htm");
        }
    }
}
```

剩下的就是页面处理验证信息回显（freemarker 模板语法）

```html
<form action="/login.do" method="post">
    <!-- 帐号的验证结果 -->
    帐号:<input name="account" type="text" value="${loginForm.account}">
    <#if validData["account"]?? >
        ${validData["account"]?join(",")}
    </#if>

    <!-- 密码的验证结果 -->
    密码:<input name="password" type="password" value="${loginForm.password}">
    <#if validData["password"]?? >
        ${validData["password"]?join(",")}
    </#if>
    <input type="submit" value="递交"/>
</form>
```