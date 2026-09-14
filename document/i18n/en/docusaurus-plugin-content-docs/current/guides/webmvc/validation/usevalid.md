---
id: usevalid
sidebar_position: 1
title: 4.5.1 Using Validators
description: Validate request parameters before request handling.
---

# 4.5.1 Using Validators

After a request is submitted to the backend and before it is formally processed, parameter validity is often checked. For example: age must be greater than 1, gender must be male or female, and account/password fields cannot be empty.

Validation information also needs to be fed back to the page. Hasor validators help implement these features.

Using login as an example, first define a request parameter group:

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

Write the validator:

```java
public class LoginFormValidation implements Validation<LoginForm> {
    public void doValidation(String validType,
            LoginForm dataForm,
            ValidInvoker errors) {
        if (StringUtils.isBlank(dataForm.getLogin())) {
            errors.addError("login", "Account cannot be empty.");
            return;
        }
        if (StringUtils.isBlank(dataForm.getPassword())) {
            errors.addError("password", "Password cannot be empty.");
            return;
        }
    }
}
```

Finally, inject `ValidInvoker` and explicitly call `doValid(...)`. The current Hasor Web codebase does not provide an `@Valid` parameter annotation; validation is triggered through `ValidInvoker`.

```java
@MappingTo("/login.htm")
public class Login {
    @Any
    public void execute(@ParameterGroup LoginForm loginForm,
            RenderInvoker invoker,
            ValidInvoker valid) {
        valid.doValid("login", loginForm);

        if (valid.isValid()) {
            invoker.renderTo("/userInfo.htm");
        } else {
            invoker.put("loginForm", loginForm);
            invoker.renderTo("/login.htm");
        }
    }
}
```

`ValidInvoker` stores validation results in `validData`, which can be read by the template engine. The following example uses FreeMarker template syntax.

```html
<form action="/login.do" method="post">
    <!-- Validation result for the account field. -->
    Account:<input name="account" type="text" value="${loginForm.account}">
    <#if validData["account"]?? >
        ${validData["account"]?join(",")}
    </#if>

    <!-- Validation result for the password field. -->
    Password:<input name="password" type="password" value="${loginForm.password}">
    <#if validData["password"]?? >
        ${validData["password"]?join(",")}
    </#if>
    <input type="submit" value="Submit"/>
</form>
```
