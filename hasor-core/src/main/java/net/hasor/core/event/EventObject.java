/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.event;
import net.hasor.core.EventCallBackHook;
import net.hasor.core.FireType;

/**
 * 用于封装事件对象。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-3-13
 */
public class EventObject<T> {
    private String               eventType = null;
    private FireType             fireType  = null;
    private T                    eventData = null;
    private EventCallBackHook<T> callBack  = null;

    public EventObject(final String eventType, final FireType fireType) {
        this.eventType = eventType;
        this.fireType = fireType;
    }

    /** 获得事件类型。 */
    public String getEventType() {
        return this.eventType;
    }

    public FireType getFireType() {
        return this.fireType;
    }

    //
    public void setCallBack(final EventCallBackHook<T> callBack) {
        this.callBack = callBack;
    }

    public EventCallBackHook<T> getCallBack() {
        return this.callBack;
    }

    public T getEventData() {
        return eventData;
    }

    public void setEventData(T eventData) {
        this.eventData = eventData;
    }
}
