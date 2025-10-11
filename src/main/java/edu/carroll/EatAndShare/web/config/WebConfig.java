package edu.carroll.EatAndShare.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import java.nio.file.Paths;

/**
 * Web configuration class for handling static file resources in the EatAndShare application.
 *
 * <p>This configuration maps uploaded files (e.g., recipe images)
 * to a public web-accessible directory. It ensures that any file
 * saved under the configured upload directory can be served through
 * URLs starting with <strong>/uploads/</strong>.</p>
 *
 * <p>For example, if a file is uploaded to:</p>
 * <pre>
 *     /home/dre/finalproject/uploads/recipe1.png
 * </pre>
 * it can be accessed through the browser at:
 * <pre>
 *     http://localhost:8080/uploads/recipe1.png
 * </pre>
 *
 * <p>The actual directory path is configurable via the
 * <strong>file.upload-dir</strong> property in
 * <code>application.properties</code> or <code>application.yml</code>.</p>
 *
 * @author Selin
 * @version 1.0
 * @since 2025-10-11
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * The root directory for uploaded files, injected from application properties.
     * <p>Example property in <code>application.properties</code>:</p>
     * <pre>
     *     file.upload-dir=uploads
     * </pre>
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Adds custom resource handlers to serve files from the local file system.
     * <p>This method maps the virtual path <strong>/uploads/**</strong>
     * to the physical directory specified by <code>file.upload-dir</code>.</p>
     *
     * @param registry the {@link ResourceHandlerRegistry} used to register resource mappings
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve the absolute path of the upload directory
        String absolutePath = Paths.get(uploadDir).toFile().getAbsolutePath() + File.separator;

        // Map requests to /uploads/** to files stored in the upload directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
