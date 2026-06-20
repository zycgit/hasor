---
id: multivalid
sidebar_position: 2
title: b. Validating with Multiple Validators
description: Compose common and scenario-specific validation logic.
---

# Validating with Multiple Validators

Some validation logic is common and can be extracted into shared validation logic. Request validation can then consist of common plus specific parts, as shown below:

```java
@ValidBy({LoginFormValidation.class, DataBaseValidation.class})
public class LoginForm {
    ...
}
```

Scenario-based validation means that during validation, developers can use the scenario name passed to the form validator to perform necessary logic checks:
- `doValidLogin`: handles login.
- `doValidSignUp`: handles registration.

```java
public class LoginFormValidation4Scene implements Validation<LoginForm4Scene> {
    // Login validation.
    private void doValidLogin(LoginForm4Scene dataForm, ValidInvoker errors) {
        ...
    }

    // Signup validation.
    private void doValidSignUp(LoginForm4Scene dataForm, ValidInvoker errors) {
        ...
    }

    public void doValidation(String validType, LoginForm4Scene dataForm, ValidInvoker errors) {
        // Common validation logic.
        if (StringUtils.isBlank(dataForm.getAccount())) {
            errors.addError("account", "Account is empty.");
        }
        if (StringUtils.isBlank(dataForm.getPassword())) {
            errors.addError("password", "Password is empty.");
        }
        if (!errors.isValid()) {
            return;
        }

        // Scenario differences.
        if (StringUtils.equalsIgnoreCase("signup", validType)) {
            this.doValidSignUp(dataForm, errors);   // Signup.
            return;
        }
        if (StringUtils.equalsIgnoreCase("login", validType)) {
            this.doValidLogin(dataForm, errors);    // Login.
            return;
        }
    }
}
```

Finally, when using validation, set the scenario name on the `@Valid` annotation.

```java
@MappingTo("/scene/login.do")
public class Login4Scene {
    public void execute(@Valid("login") @ParameterGroup LoginForm4Scene loginForm,
            RenderInvoker invoker,
            ValidInvoker valid) {
        ...
    }
}
```
