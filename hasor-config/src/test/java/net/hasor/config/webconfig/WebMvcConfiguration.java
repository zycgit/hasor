package net.hasor.config.webconfig;

import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.config.Configuration;
import net.hasor.config.web.CorsRegistry;
import net.hasor.config.web.JsonRenderConfigurer;
import net.hasor.config.web.ResourceHandlerRegistry;
import net.hasor.config.web.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    public static final AtomicInteger RESOURCE_CONFIGURES = new AtomicInteger();
    public static final AtomicInteger CORS_CONFIGURES     = new AtomicInteger();
    public static final AtomicInteger JSON_CONFIGURES     = new AtomicInteger();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        RESOURCE_CONFIGURES.incrementAndGet();
        registry.addResourceHandler("/assets/**")//
                .addResourceLocations("classpath:/web-assets/")//
                .setWelcomeFile("home.html")//
                .setOrder(-100);
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
        configurer.useDefaultJsonRenderEngine();
    }
}
