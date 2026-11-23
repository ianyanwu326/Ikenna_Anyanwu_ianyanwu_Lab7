package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identifies the student to take off a course roster.")
public record RosterChangeRequest(
        @Schema(description = "Id of the student.", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long studentId) {
}
