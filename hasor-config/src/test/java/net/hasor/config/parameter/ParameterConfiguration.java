/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.config.parameter;
import java.util.List;
import net.hasor.config.Bean;
import net.hasor.config.Configuration;
import net.hasor.core.Inject;
import net.hasor.core.Type;

@Configuration
public class ParameterConfiguration {
    @Bean(singleton = false)
    public List<String> selected(//
            String main, //
            @Inject String defaultSource, //
            @Inject(value = "", byType = Type.ByID) String emptyQualifier, //
            @Inject("first") String first, //
            @Inject("second") String second,//
            @Inject(value = "reference", byType = Type.ByID) String byId) {
        return List.of(main, defaultSource, emptyQualifier, first, second, byId);
    }
}
