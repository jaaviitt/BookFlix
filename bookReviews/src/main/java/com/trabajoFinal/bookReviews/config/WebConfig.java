package com.trabajoFinal.bookReviews.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapeamos la URL "/uploads/**" a la carpeta física "uploads/"
        // "file:uploads/" le dice a Spring que busque en el sistema de archivos, no en el classpath
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}