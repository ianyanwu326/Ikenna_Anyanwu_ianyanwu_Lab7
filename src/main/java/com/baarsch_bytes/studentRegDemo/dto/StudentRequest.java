package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "The fields a client supplies to create or update a student.")
public class StudentRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the student.", example = "Sherlock Holmes",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 100)
    private String name;

    @NotBlank(message = "Major is required")
    @Size(min = 1, max = 255, message = "Major must be between 1 and 255 characters")
    @Schema(description = "Declared major.", example = "Computer Science",
            requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 255)
    private String major;

    @DecimalMin(value = "0.0", message = "No negative GPAs allowed")
    @DecimalMax(value = "4.0", message = "No GPAs above 4.0 allowed")
    @Schema(description = "Grade point average on a 4.0 scale.", example = "3.75",
            requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0.0", maximum = "4.0")
    private Double gpa;

    @ArraySchema(
            arraySchema = @Schema(description = "Ids of the courses to enroll this student in. "
                    + "The student is added to the roster of each course. "
                    + "On an update, the list replaces the student's current courses."),
            schema = @Schema(type = "integer", format = "int64", example = "1"))
    private Set<Long> courses;

    @Schema(description = "Date of birth. Protected student data: it is accepted on write and "
            + "is never returned by the API.",
            type = "string", format = "date", example = "2004-01-06",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private LocalDate dateOfBirth;

    public StudentRequest(){}

    public StudentRequest(String name, String major, Double gpa, Set<Long> courses) {
        this.name = name;
        this.major = major;
        this.gpa = gpa;
        this.courses = courses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public Set<Long> getCourses() {
        return courses;
    }

    public void setCourses(Set<Long> courses) {
        this.courses = courses;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
