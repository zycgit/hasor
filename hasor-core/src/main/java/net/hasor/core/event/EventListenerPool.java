/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.event;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.hasor.core.EventListener;

/**
 * 用于封装事件对象。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014-3-13
 */
class EventListenerPool {
    private final Object                                 ONCE_LOCK = new Object();
    private       CopyOnWriteArrayList<EventListener<?>> onceListener;
    private final CopyOnWriteArrayList<EventListener<?>> listenerList;

    public EventListenerPool() {
        onceListener = new CopyOnWriteArrayList<>();
        listenerList = new CopyOnWriteArrayList<>();
    }

    public boolean pushOnceListener(EventListener<?> eventListener) {
        synchronized (ONCE_LOCK) {
            return onceListener.add(eventListener);
        }
    }

    public boolean addListener(EventListener<?> eventListener) {
        return listenerList.add(eventListener);
    }

    public List<EventListener<?>> popOnceListener() {
        List<EventListener<?>> onceList = null;
        synchronized (ONCE_LOCK) {
            onceList = this.onceListener;
            this.onceListener = new CopyOnWriteArrayList<>();
        }
        return onceList;
    }

    public List<EventListener<?>> getListenerSnapshot() {
        return new ArrayList<>(this.listenerList);
    }

    public boolean removeListener(EventListener<?> eventListener) {
        return listenerList.remove(eventListener);
    }

    public boolean clearListener() {
        onceListener.clear();
        listenerList.clear();
        return true;
    }
}
