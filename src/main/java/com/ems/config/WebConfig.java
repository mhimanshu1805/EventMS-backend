package com.ems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves poster files from uploads/posters in the backend working directory
 * at GET /uploads/posters/{filename}. SecurityConfig leaves that GET public.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/posters/**")
                .addResourceLocations("file:uploads/posters/");
    }
}
