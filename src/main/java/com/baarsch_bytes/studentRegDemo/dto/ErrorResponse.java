package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "The standard body of an error response.")
public record ErrorResponse(
        @Schema(description = "When the error occurred.", example = "2026-11-06T14:30:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED)
        OffsetDateTime timestamp,

        @Schema(description = "HTTP status code.", example = "404",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int status,

        @Schema(description = "HTTP status phrase.", example = "Not Found",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String error,

        @Schema(description = "What went wrong.", example = "No such student exists.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(description = "The path that was requested.", example = "/api/students/99",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String path) {
}
