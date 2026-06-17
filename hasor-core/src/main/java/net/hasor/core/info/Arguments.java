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
package net.hasor.core.info;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command-line arguments used to start a Hasor application.
 *
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
