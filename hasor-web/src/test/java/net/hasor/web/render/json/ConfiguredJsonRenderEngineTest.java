package net.hasor.web.render.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import net.hasor.web.Invoker;
import net.hasor.web.render.RenderEngine;
import net.hasor.web.render.RenderInvoker;

public class ConfiguredJsonRenderEngineTest {
    public static class Value {
        public String displayName = "Hasor";
    }

    private String render(RenderEngine engine, Object value) throws Throwable {
        RenderInvoker invoker = mock(RenderInvoker.class);
        when(invoker.get(Invoker.RETURN_DATA_KEY)).thenReturn(value);
        StringWriter writer = new StringWriter();
        engine.process(invoker, writer);
        return writer.toString();
    }

    @Test
    public void gsonUsesSuppliedSerializer() throws Throwable {
        GsonRenderEngine engine = new GsonRenderEngine(new GsonBuilder().registerTypeAdapter(Value.class, (JsonSerializer<Value>) (value, type, context) -> new JsonPrimitive("custom")).create());
        assertEquals("\"custom\"", render(engine, new Value()));
        assertEquals("{\"displayName\":\"Hasor\"}", render(new GsonRenderEngine(), new Value()));
    }

    @Test
    public void jacksonUsesSuppliedMapper() throws Throwable {
        ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        assertEquals("{\"display_name\":\"Hasor\"}", render(new JacksonRenderEngine(mapper), new Value()));
        assertEquals("{\"displayName\":\"Hasor\"}", render(new JacksonRenderEngine(), new Value()));
    }

    @Test
    public void fastjsonUsesPrivateConfigAndCopiesFeatures() throws Throwable {
        SerializeConfig config = new SerializeConfig();
        config.put(Value.class, (serializer, value, name, type, features) -> serializer.write("custom"));
        SerializerFeature[] features = { SerializerFeature.WriteMapNullValue };
        JsonRenderEngine engine = new JsonRenderEngine(config, features);
        features[0] = SerializerFeature.PrettyFormat;
        assertEquals("\"custom\"", render(engine, new Value()));
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("missing", null);
        assertEquals("{\"missing\":null}", render(engine, value));
        assertEquals("{}", render(new JsonRenderEngine(), value));
        assertEquals("{\"displayName\":\"Hasor\"}", render(new JsonRenderEngine(), new Value()));
    }

    @Test
    public void fastjson2UsesPrivateProviderAndCopiesFeatures() throws Throwable {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(Value.class, (writer, value, name, type, features) -> writer.writeString("custom"));
        JSONWriter.Feature[] features = { JSONWriter.Feature.WriteNulls };
        Fastjson2RenderEngine engine = new Fastjson2RenderEngine(provider, features);
        features[0] = JSONWriter.Feature.PrettyFormat;
        assertEquals("\"custom\"", render(engine, new Value()));
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("missing", null);
        assertEquals("{\"missing\":null}", render(engine, value));
        assertEquals("{}", render(new Fastjson2RenderEngine(), value));
        assertEquals("{\"displayName\":\"Hasor\"}", render(new Fastjson2RenderEngine(), new Value()));
    }

    @Test
    public void fastjson2ContextIsCreatedPerRender() throws Throwable {
        AtomicInteger calls = new AtomicInteger();
        Fastjson2RenderEngine engine = new Fastjson2RenderEngine(() -> {
            calls.incrementAndGet();
            return new JSONWriter.Context(new ObjectWriterProvider());
        });
        assertEquals("1", render(engine, 1));
        assertEquals("2", render(engine, 2));
        assertEquals(2, calls.get());
    }

    @Test
    public void fastjson2NullContextUsesDefaultSerialization() throws Throwable {
        Fastjson2RenderEngine engine = new Fastjson2RenderEngine(() -> null);
        assertEquals("{\"displayName\":\"Hasor\"}", render(engine, new Value()));
        assertEquals("null", render(engine, null));
    }

    @Test
    public void fastjson2ContextCanChangeBetweenRenders() throws Throwable {
        AtomicInteger calls = new AtomicInteger();
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(Value.class, (writer, value, name, type, features) -> writer.writeString("custom"));
        Fastjson2RenderEngine engine = new Fastjson2RenderEngine(() -> calls.getAndIncrement() == 1 ? null : new JSONWriter.Context(provider));
        assertEquals("\"custom\"", render(engine, new Value()));
        assertEquals("{\"displayName\":\"Hasor\"}", render(engine, new Value()));
        assertEquals("\"custom\"", render(engine, new Value()));
        assertEquals(3, calls.get());
    }

    @Test
    public void missingConfigurationIsRejected() {
        assertNullRejected(() -> new GsonRenderEngine(null));
        assertNullRejected(() -> new JacksonRenderEngine(null));
        assertNullRejected(() -> new JsonRenderEngine(null));
        assertNullRejected(() -> new JsonRenderEngine(new SerializeConfig(), (SerializerFeature[]) null));
        assertNullRejected(() -> new Fastjson2RenderEngine((ObjectWriterProvider) null));
        assertNullRejected(() -> new Fastjson2RenderEngine(new ObjectWriterProvider(), (JSONWriter.Feature[]) null));
    }

    private void assertNullRejected(Runnable action) {
        try {
            action.run();
            fail("null configuration must be rejected");
        } catch (NullPointerException expected) {
            // 构造时报告错误，而不是等到请求到达。
        }
    }
}
