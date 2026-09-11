/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.valid;
import java.io.Serializable;

/**
 * 消息。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年10月25日
 */
public class Message implements Serializable {
    private static final long     serialVersionUID = -4678293554960623786L;
    private              String   messageTemplate  = null;
    private              Object[] messageParams    = null;

    public Message(String message) {
        this(message, new Object[0]);
    }

    public Message(String messageTemplate, Object... messageParams) {
        this.messageTemplate = messageTemplate;
        this.messageParams = messageParams == null ? new Object[0] : messageParams;
    }

    /** 获取消息模版信息。 */
    public String getMessageTemplate() {
        return this.messageTemplate;
    }

    /** 获取消息 */
    public String getMessage() {
        try {
            if (this.messageParams != null && this.messageParams.length > 0) {
                return String.format(this.messageTemplate, this.messageParams);
            } else {
                return messageTemplate;
            }
        } catch (Exception e) {
            return this.messageTemplate;
        }
    }

    /** 获取参数 */
    public Object[] getParameters() {
        return this.messageParams;
    }

    public String toString() {
        return this.getMessage();
    }
}
