package net.hasor.config;

import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.web.WebApiBinder;
import org.junit.Test;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

public class ConfigurationScannerOrderTest {
    @Test
    public void moduleTraversesOnlyCoreScopeOnce() throws Throwable {
        WebApiBinder binder = mock(WebApiBinder.class);
        Settings settings = mock(Settings.class);
        when(binder.getSettings()).thenReturn(settings);
        when(settings.getString("hasor.loadPackages", "")).thenReturn(" example.app, example.extra ");
        when(binder.tryCast(WebApiBinder.class)).thenReturn(binder);

        net.hasor.cobble.loader.ResourceLoader resources = mock(net.hasor.cobble.loader.ResourceLoader.class);
        when(binder.getResourceLoader()).thenReturn(resources);
        ConfigurationModule.auto().loadModule(binder);

        verify(resources, times(1)).scanResources(eq(net.hasor.cobble.loader.MatchType.Prefix), any(net.hasor.cobble.loader.Scanner.class), aryEq(new String[] { "example/app", "example/extra" }));
    }

    @Test
    public void emptyScopeDoesNotInvokeScanners() throws Throwable {
        ApiBinder binder = mock(ApiBinder.class);
        Settings settings = mock(Settings.class);
        when(binder.getSettings()).thenReturn(settings);
        when(settings.getString("hasor.loadPackages", "")).thenReturn(" , ");

        ConfigurationModule.auto().loadModule(binder);

        verify(binder).getSettings();
        verifyNoMoreInteractions(binder);
    }
}
