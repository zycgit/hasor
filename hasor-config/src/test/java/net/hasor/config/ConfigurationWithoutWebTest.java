package net.hasor.config;

import java.net.URL;
import java.net.URLClassLoader;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.Hasor;
import net.hasor.core.Module;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConfigurationWithoutWebTest {
    @Test
    public void automaticConfigurationLoadsWithoutWebClasses() throws Throwable {
        URL location = ConfigurationModule.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[] { location }, getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                synchronized (getClassLoadingLock(name)) {
                    if (name.startsWith("net.hasor.web.") || name.startsWith("javax.servlet.")) {
                        throw new ClassNotFoundException(name);
                    }
                    if (name.startsWith("net.hasor.config.")) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) {
                            type = findClass(name);
                        }
                        if (resolve) {
                            resolveClass(type);
                        }
                        return type;
                    }
                    return super.loadClass(name, resolve);
                }
            }
        }) {
            Module module = (Module) loader.loadClass(ConfigurationModule.class.getName()).getConstructor().newInstance();
            assertSame(loader, module.getClass().getClassLoader());
            ApiBinder binder = mock(ApiBinder.class);
            when(binder.getSettings()).thenReturn(Hasor.create()
                    .addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "example.application.*").buildSettings());
            when(binder.getResourceLoader()).thenReturn(mock(net.hasor.cobble.loader.ResourceLoader.class));
            when(binder.getClassLoader()).thenReturn(loader);
            module.loadModule(binder);
            verify(binder).getResourceLoader();
            verify(binder, never()).tryCast(any());
        }
    }
}
