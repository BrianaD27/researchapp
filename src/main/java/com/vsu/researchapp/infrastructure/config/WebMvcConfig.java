package com.vsu.researchapp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; 
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration 
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local")
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.storage.local.base-path}")
    // ^ same property LocalFileStorageService reads, so both point at the same folder on disk
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            // ^ any request whose path starts with "/uploads/" is handled as a static resource request instead of being routed to a @RestController
            .addResourceLocations("file:" + basePath + "/");
            // ^ "file:" tells Spring to resolve this against the filesystem (not the classpath); the trailing "/" is required for Spring to correctly treat basePath as a directory
    }
}
