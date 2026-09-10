package net.hasor.config.webconfig;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.loader.providers.PrefixResourceLoader;
import net.hasor.config.Configuration;
import net.hasor.config.web.WebMvcConfigurer;
import net.hasor.config.web.cors.CorsRegistry;
import net.hasor.config.web.render.JsonRenderConfigurer;
import net.hasor.web.WebApiBinder;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    public static final AtomicInteger RESOURCE_CONFIGURES = new AtomicInteger();
    public static final AtomicInteger CORS_CONFIGURES     = new AtomicInteger();
    public static final AtomicInteger JSON_CONFIGURES     = new AtomicInteger();

    @Override
    public void addResourceHandlers(WebApiBinder binder) {
        RESOURCE_CONFIGURES.incrementAndGet();
        binder.addResource("/assets/**", new PrefixResourceLoader(binder.getResourceLoader(), "web-assets"))//
                .welcomeFile("home.html")//
                .order(-100);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CORS_CONFIGURES.incrementAndGet();
        registry.addMapping("/api/**")//
                .allowedOrigins("https://hasor.net")//
                .allowedMethods("GET", "POST")//
                .allowedHeaders("content-type", "authorization")//
                .allowCredentials(true)//
                .maxAge(1800)//
                .setOrder(-90);
    }

    @Override
    public void configureJson(JsonRenderConfigurer configurer) {
        JSON_CONFIGURES.incrementAndGet();
        configurer.renderEngine(net.hasor.web.render.json.JsonRenderEngine.class);
    }
}
