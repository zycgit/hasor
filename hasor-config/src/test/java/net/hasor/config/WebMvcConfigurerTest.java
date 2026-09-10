package net.hasor.config;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import javax.servlet.ServletContext;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import net.hasor.cobble.setting.Settings;
import net.hasor.config.web.cors.CorsFilter;
import net.hasor.config.webconfig.WebMvcConfiguration;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.Hasor;
import net.hasor.web.InvokerFilter;
import net.hasor.web.binder.FilterDef;
import net.hasor.web.binder.RenderDef;
import net.hasor.web.render.json.JsonRenderEngine;

public class WebMvcConfigurerTest {
    @Test
    public void configurationShouldRegisterWebMvcComponentsDuringModuleLoading() throws Throwable {
        WebMvcConfiguration.RESOURCE_CONFIGURES.set(0);
        WebMvcConfiguration.CORS_CONFIGURES.set(0);
        WebMvcConfiguration.JSON_CONFIGURES.set(0);

        ServletContext servletContext = PowerMockito.mock(ServletContext.class);
        PowerMockito.when(servletContext.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        PowerMockito.when(servletContext.getContextPath()).thenReturn("/");
        PowerMockito.when(servletContext.getEffectiveMajorVersion()).thenReturn(3);
        PowerMockito.when(servletContext.getVirtualServerName()).thenReturn("test");

        AppContext context = Hasor.create(servletContext)//
                .addSettings(Settings.DefaultNameSpace, "hasor.loadPackages", "net.hasor.config.webconfig")//
                .build();

        assertEquals(1, WebMvcConfiguration.RESOURCE_CONFIGURES.get());
        assertEquals(1, WebMvcConfiguration.CORS_CONFIGURES.get());
        assertEquals(1, WebMvcConfiguration.JSON_CONFIGURES.get());

        List<FilterDef> filters = context.findBindingBean(FilterDef.class);
        assertEquals(1, filters.size());
        assertTrue(filters.stream().map(filter -> newFilter(context, filter)).anyMatch(CorsFilter.class::isInstance));

        List<RenderDef> renders = context.findBindingBean(RenderDef.class).stream().filter(def -> !def.isFallback()).toList();
        assertEquals(1, renders.size());
        assertEquals("json", renders.get(0).getRenderName());
        assertTrue(renders.get(0).newEngine(context) instanceof JsonRenderEngine);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static InvokerFilter newFilter(AppContext context, FilterDef filter) {
        return (InvokerFilter) context.getInstance((BindInfo) filter.getTargetType());
    }
}
