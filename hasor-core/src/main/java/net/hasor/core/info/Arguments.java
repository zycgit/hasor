/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.core.info;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command-line arguments used to start a Hasor application.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
public record Arguments(String[] args) {
    public static final String MAIN_ARGS = "mainArgs";

    public Arguments(String[] args) {
        this.args = args == null ? new String[0] : args.clone();
    }

    @Override
    public String[] args() {
        return this.args.clone();
    }

    public List<String> getArgList() {
        return Collections.unmodifiableList(Arrays.asList(args()));
    }

    @Override
    public String toString() {
        return Arrays.toString(this.args);
    }
}
