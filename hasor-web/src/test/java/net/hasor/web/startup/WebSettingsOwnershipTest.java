package net.hasor.web.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import net.hasor.cobble.setting.Settings;
import net.hasor.core.Hasor;

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