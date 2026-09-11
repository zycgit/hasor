/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.core.mods;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.Module;

public class EmptyLifeModule implements Module {
    private boolean doStart = false;
    private boolean doStop  = false;
    private boolean doLoad  = false;

    public boolean isDoStart() {
        return doStart;
    }

    public boolean isDoStop() {
        return doStop;
    }

    public boolean isDoLoad() {
        return doLoad;
    }

    @Override
    public void onStart(AppContext appContext) throws Throwable {
        this.doStart = true;
    }

    @Override
    public void onStop(AppContext appContext) throws Throwable {
        this.doStop = true;
    }

    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        this.doLoad = true;
    }
}
