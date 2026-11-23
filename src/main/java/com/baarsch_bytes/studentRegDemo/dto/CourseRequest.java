package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "The fields a client supplies to create or update a course.")
public class CourseRequest {

    @Schema(description = "Name of the course.", example = "CS1",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String name;

    @Schema(description = "Id of the instructor teaching the course.", example = "2",
            minimum = "1")
    private Long instructor;

    @Schema(description = "Maximum number of students the course can hold. "
            + "It cannot be set below the number of students already on the roster.",
            example = "30", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    private Integer maxSize;

    @Schema(description = "Room where the course meets.", example = "MCS 339",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String room;

    @ArraySchema(
            arraySchema = @Schema(description = "Ids of the students to place on the roster. "
                    + "When omitted from an update, the roster is left unchanged. "
                    + "Every id must belong to an existing student."),
            schema = @Schema(type = "integer", format = "int64", example = "1"))
    private Set<Long> roster;

    public CourseRequest(){}
    public CourseRequest(String name, Long instructor, Integer maxSize,
                         String room, Set<Long> roster) {
        this.name = name;
        this.instructor = instructor;
        this.maxSize = maxSize;
        this.room = room;
        this.roster = roster;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getInstructor() {
        return instructor;
    }

    public void setInstructor(Long instructor) {
        this.instructor = instructor;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Set<Long> getRoster() {
        return roster;
    }

    public void setRoster(Set<Long> roster) {
        this.roster = roster;
    }
}
