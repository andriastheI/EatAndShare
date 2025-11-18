package edu.carroll.eatAndShare.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Filename: WebConfig.java
 * Author: Andrias, Selin
 * Date: October 20, 2025
 *
 * Description:
 * Web MVC configuration used to serve uploaded static resources such as
 * recipe images. This class maps the local upload directory to the public
 * URL path "/uploads/**", allowing images saved on the file system to be
 * accessible through the browser.
 *
 * The physical location of uploaded files is controlled through the
 * "file.upload-dir" property in application.properties.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Directory where uploaded files are stored.
     * Injected from application.properties using the key:
     * file.upload-dir=uploads
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Registers a resource handler so that files stored in the upload directory
     * can be served statically via URLs beginning with "/uploads/**".
     *
     * Example:
     * If an image is stored at:
     *     uploads/cake.png
     *
     * It can be accessed at:
     *     http://localhost:8080/uploads/cake.png
     *
     * @param registry the registry used to define resource mappings
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Resolve absolute filesystem path to the upload directory
        String absolutePath = Paths.get(uploadDir)
                .toFile()
                .getAbsolutePath() + File.separator;

        // Map virtual "/uploads/**" URL requests to actual files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
