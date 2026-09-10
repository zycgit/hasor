package net.hasor.web.render.json;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderInvoker;

public class AutoJsonRenderEngineTest {
    private static final String[] LIBRARIES = { "com.fasterxml.jackson.", "com.google.gson.", "com.alibaba.fastjson.", "com.alibaba.fastjson2." };

    /** Define adapters in the filtered loader so their optional imports are filtered too. */
    private ClassLoader environment(int... present) {
        return new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                for (int i = 0; i < LIBRARIES.length; i++) {
                    if (!name.startsWith(LIBRARIES[i])) {
                        continue;
                    }
                    boolean allowed = false;
                    for (int index : present) {
                        if (index == i) {
                            allowed = true;
                        }
                    }
                    if (!allowed) {
                        throw new ClassNotFoundException(name);
                    }
                }
                if (name.matches("net\\.hasor\\.web\\.render\\.json\\.(Jackson|Gson|Json|Fastjson2)RenderEngine")) {
                    Class<?> found = findLoadedClass(name);
                    if (found == null) {
                        try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                            byte[] bytes = input.readAllBytes();
                            found = defineClass(name, bytes, 0, bytes.length);
                        } catch (Exception e) {
                            throw new ClassNotFoundException(name, e);
                        }
                    }
                    if (resolve) {
                        resolveClass(found);
                    }
                    return found;
                }
                return super.loadClass(name, resolve);
            }
        };
    }

    private void assertProvider(String expected, int... present) throws Throwable {
        AutoJsonRenderEngine engine = new AutoJsonRenderEngine(environment(present));
        engine.initialize();
        Field delegate = AutoJsonRenderEngine.class.getDeclaredField("delegate");
        delegate.setAccessible(true);
        Object selected = delegate.get(engine);
        assertEquals(expected, selected.getClass().getSimpleName());
        RenderInvoker invoker = PowerMockito.mock(RenderInvoker.class);
        PowerMockito.when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(Map.of("name", "中文"));
        StringWriter writer = new StringWriter();
        engine.process(invoker, writer);
        assertEquals("{\"name\":\"中文\"}", writer.toString());
        PowerMockito.when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(List.of(1, 2));
        writer = new StringWriter();
        engine.process(invoker, writer);
        assertEquals("[1,2]", writer.toString());
        PowerMockito.when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(null);
        writer = new StringWriter();
        engine.process(invoker, writer);
        assertEquals("null", writer.toString());
        assertSame(selected, delegate.get(engine));
    }

    @Test
    public void jacksonOnly() throws Throwable {
        assertProvider("JacksonRenderEngine", 0);
    }

    @Test
    public void gsonOnly() throws Throwable {
        assertProvider("GsonRenderEngine", 1);
    }

    @Test
    public void fastjsonOnly() throws Throwable {
        assertProvider("JsonRenderEngine", 2);
    }

    @Test
    public void fastjson2Only() throws Throwable {
        assertProvider("Fastjson2RenderEngine", 3);
    }

    @Test
    public void deterministicPriority() throws Throwable {
        assertProvider("JacksonRenderEngine", 0, 1, 2, 3);
        assertProvider("GsonRenderEngine", 1, 2, 3);
        assertProvider("JsonRenderEngine", 2, 3);
    }

    @Test
    public void missingLibrariesHaveActionableError() {
        try {
            new AutoJsonRenderEngine(environment()).initialize();
            fail("Should require a JSON library");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("No JSON provider found"));
        }
    }

    @Test
    public void brokenLibraryDoesNotSilentlyChangeSerialization() {
        ClassLoader broken = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals("com.fasterxml.jackson.databind.ObjectMapper")) {
                    throw new NoClassDefFoundError("missing dependency");
                }
                return super.loadClass(name, resolve);
            }
        };
        try {
            new AutoJsonRenderEngine(broken).initialize();
            fail("Broken Jackson must not silently select Gson");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getCause() instanceof LinkageError);
        }
    }
}
