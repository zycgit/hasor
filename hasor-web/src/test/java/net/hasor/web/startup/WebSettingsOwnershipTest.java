/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.web.startup;

import net.hasor.cobble.setting.Settings;
import net.hasor.core.Hasor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WebSettingsOwnershipTest {
    @Test
    public void standaloneWebDoesNotSupplyEmbeddedServerSettings() {
        assertNull(getClass().getClassLoader().getResource("META-INF/hasor-framework/boot-web-hconfig.xml"));
        Settings settings = Hasor.create().buildSettings();
        assertNull(settings.getString("hasor.http.server"));
        assertNull(settings.getString("hasor.http.host"));
        assertNull(settings.getString("hasor.http.port"));
        assertNull(settings.getString("hasor.http.contextPath"));
        assertEquals("json", settings.getString("hasor.render.defaults.objectEngine"));
    }
}
