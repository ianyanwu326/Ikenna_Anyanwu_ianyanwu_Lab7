package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The short form of a student used in a course roster.")
public record StudentSummary(
        @Schema(description = "Unique id of the student.", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Full name of the student.", example = "Sherlock Holmes",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String name) {
}
