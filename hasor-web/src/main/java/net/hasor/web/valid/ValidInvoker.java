/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.valid;
import net.hasor.web.Invoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表单验证框架Api接口
 * @version : 2017-01-10
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ValidInvoker extends Invoker {
    String VALID_DATA_KEY = "validData";//

    /** 验证失败的验证keys */
    List<String> validKeys();

    /** Message of string */
    default List<String> validErrorsOfString() {
        return validErrorsOfMessage()       //
                .stream()                   //
                .map(Message::toString)     //
                .collect(Collectors.toList());
    }

    /** 获取某个key下验证失败信息 */
    default List<String> validErrorsOfString(String key) {
        return validErrorsOfMessage(key)    //
                .stream()                   //
                .map(Message::toString)     //
                .collect(Collectors.toList());
    }

    /** 获取某个key下验证失败信息 */
    default String firstValidErrorsOfString(String key) {
        Message msg = firstValidErrorsOfMessage(key);
        return (msg != null) ? msg.toString() : null;
    }

    /** 获取所有验证失败信息 */
    default List<Message> validErrorsOfMessage() {
        List<Message> finalMessage = new ArrayList<>();
        for (String key : validKeys()) {
            finalMessage.addAll(validErrorsOfMessage(key));
        }
        return finalMessage;
    }

    /** 获取某个key下验证失败信息 */
    List<Message> validErrorsOfMessage(String key);

    /** 获取某个key下验证失败信息 */
    Message firstValidErrorsOfMessage(String key);

    /** 是否通过验证。*/
    boolean isValid();

    /** 某个规则是否通过验证 */
    boolean isValid(String key);

    /** 删除所有验证信息 */
    default void clearValidErrors() {
        for (String key : validKeys()) {
            clearValidErrors(key);
        }
    }

    /** 删除某个验证信息 */
    void clearValidErrors(String key);

    /** 添加验证失败的消息 */
    default void addError(String key, String validString) {
        this.addError(key, new Message(validString));
    }

    /** 添加验证失败的消息 */
    default void addError(String key, String validString, Object... args) {
        this.addError(key, new Message(validString, args));
    }

    /** 添加验证失败的消息 */
    default void addError(String key, Message validMessage) {
        addErrors(key, Collections.singletonList(validMessage));
    }

    /** 添加验证失败的消息 */
    void addErrors(String key, List<Message> validMessage);

    default boolean doValid(String scene, Object object) {
        if (object == null) {
            return false;
        }
        ValidBy[] byType = object.getClass().getAnnotationsByType(ValidBy.class);
        if (byType == null || byType.length == 0) {
            return false;
        }
        Class<?>[] collect = Arrays.stream(byType).flatMap((Function<ValidBy, Stream<?>>) validBy -> {
            return Arrays.stream(validBy.value());
        }).toArray(Class<?>[]::new);
        return this.doValid(scene, object, (Class<? extends Validation>[]) collect);
    }

    default boolean doValid(String scene, Object object, Class<? extends Validation>... validArrays) {
        for (Class<? extends Validation> validType : validArrays) {
            Validation validation = getAppContext().getInstance(validType);
            if (validation == null) {
                throw new NullPointerException("create " + validType.getName() + " Validation failed , return null.");
            }
            validation.doValidation(scene, object, this);
        }
        return this.isValid();
    }
}