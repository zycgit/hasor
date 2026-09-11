/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.event;
import net.hasor.core.AppContext;
import net.hasor.core.EventListener;

public class AppContextListener implements EventListener<AppContext> {
    private String     event;
    private AppContext appContext;
    private int        count;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void onEvent(String event, AppContext eventData) throws Throwable {
        this.event = event;
        this.appContext = eventData;
        this.count++;
    }
}
