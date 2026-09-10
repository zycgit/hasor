package net.hasor.web.invoker;
import static org.junit.Assert.*;
import java.lang.reflect.Modifier;
import org.junit.Test;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.web.AbstractTest;
import net.hasor.web.binder.ResourceDef;

public class ResourceProcessorTest extends AbstractTest {
    @Test
    public void processorIsPackagePrivateAndNotAContainerBean() throws Throwable {
        assertFalse(Modifier.isPublic(ResourceProcessor.class.getModifiers()));
        assertFalse(Modifier.isPublic(ResourceHandler.class.getModifiers()));
        try (AppContext app = Hasor.create(servlet30("/")).build()) {
            assertTrue(app.findBindingBean(ResourceProcessor.class).isEmpty());
            assertTrue(app.findBindingBean(ResourceHandler.class).isEmpty());
            assertEquals(0, app.getInstance(ResourceDef[].class).length);
        }
    }
}