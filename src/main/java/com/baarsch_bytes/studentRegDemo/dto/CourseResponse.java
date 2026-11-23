package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "A course as returned by the API.")
public class CourseResponse {

    @Schema(description = "Unique id assigned by the service.", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Name of the course.", example = "CS1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "instructorId", description = "Id of the instructor teaching the course.",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long instructor;

    @Schema(description = "Maximum number of students the course can hold.", example = "30",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxSize;

    @Schema(description = "Room where the course meets.", example = "MCS 339",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String room;

    @ArraySchema(
            arraySchema = @Schema(description = "The students on the roster, with one entry per student.",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(implementation = StudentSummary.class))
    private Set<String> roster;

    @Schema(description = "Whether the course is accepting enrollments.",
            allowableValues = {"OPEN", "CLOSED"}, example = "OPEN",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    public CourseResponse(){}
    public CourseResponse(Long id, String name, Long instructor, Integer maxSize,
                          String room, Set<String> roster) {
        this.id = id;
        this.name = name;
        this.instructor = instructor;
        this.maxSize = maxSize;
        this.room = room;
        this.roster = roster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<String> getRoster() {
        return roster;
    }

    public void setRoster(Set<String> roster) {
        this.roster = roster;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
