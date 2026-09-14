---
id: multivalid
sidebar_position: 2
title: 4.5.2 多验证器共同验证
description: Hasor 框架开发手册，覆盖 hasor-core、hasor-web、hasor-boot 的核心用法
---

# 4.5.2 多验证器共同验证

有些校验逻辑比较通用，可以提取成公共的校验逻辑。这样请求验证就可以是 公共 + 制定 两部分组成，如下：

```java
@ValidBy({LoginFormValidation.class, DataBaseValidation.class})
public class LoginForm {
    ...
}
```

验证场景化，是指在执行验证时。开发者可以通过传给表单验证器的场景名称，进行必要的逻辑判断：
- doValidLogin、负责处理登录
- doValidSignUp、负责处理注册

```java
public class LoginFormValidation4Scene implements Validation<LoginForm4Scene> {
    // - 登录验证
    private void doValidLogin(LoginForm4Scene dataForm, ValidInvoker errors) {
        ...
    }

    // - 注册登录
    private void doValidSignUp(LoginForm4Scene dataForm, ValidInvoker errors) {
        ...
    }

    public void doValidation(String validType, LoginForm4Scene dataForm, ValidInvoker errors) {
        // -通用验证逻辑
        if (StringUtils.isBlank(dataForm.getAccount())) {
            errors.addError("account", "帐号为空。");
        }
        if (StringUtils.isBlank(dataForm.getPassword())) {
            errors.addError("password", "密码为空。");
        }
        if (!errors.isValid()) {
            return;
        }

        // -场景化差异
        if (StringUtils.equalsIgnoreCase("signup", validType)) {
            this.doValidSignUp(dataForm, errors);   // 注册
            return;
        }
        if (StringUtils.equalsIgnoreCase("login", validType)) {
            this.doValidLogin(dataForm, errors);    // 登录
            return;
        }
    }
}
```

最后，在使用验证时，通过 `ValidInvoker#doValid(scene, object)` 传入场景名称即可。

```java
@MappingTo("/scene/login.do")
public class Login4Scene {
    @Any
    public void execute(@ParameterGroup LoginForm4Scene loginForm,
            RenderInvoker invoker,
            ValidInvoker valid) {
        valid.doValid("login", loginForm);
        ...
    }
}
```
