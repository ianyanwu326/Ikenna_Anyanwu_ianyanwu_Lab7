package com.baarsch_bytes.studentRegDemo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

// Top-level information for the OpenAPI contract.  springdoc combines this with the
// annotations on the controllers and DTOs to produce the document at /v3/api-docs.
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Student Registration API",
                version = "1.0.0",
                description = """
                        Manages students, courses, and course rosters.

                        All error responses use the `ErrorResponse` schema unless an operation says otherwise.
                        Data is stored in memory and is cleared each time the service restarts."""),
        servers = @Server(url = "http://localhost:8080", description = "Local development server"),
        tags = {
                @Tag(name = "Students", description = "Create, read, update, and delete students"),
                @Tag(name = "Courses", description = "Create, read, update, and delete courses"),
                @Tag(name = "Rosters", description = "Enroll students in courses and report enrollment")
        })
public class OpenApiConfig {
}
