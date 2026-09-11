/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.test.web.spi;
import java.util.ArrayList;
import net.hasor.web.Mapping;
import net.hasor.web.spi.MappingDiscoverer;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2017-01-08
 */
public class TestMappingDiscoverer implements MappingDiscoverer {
    private final ArrayList<Mapping> mappings = new ArrayList<>();

    public ArrayList<Mapping> getMappings() {
        return mappings;
    }

    @Override
    public void discover(Mapping mappingData) {
        this.mappings.add(mappingData);
    }
}
