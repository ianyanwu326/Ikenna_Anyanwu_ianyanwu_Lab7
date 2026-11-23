package com.baarsch_bytes.studentRegDemo.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "A student as returned by the API.")
public class StudentResponse {

    @Schema(description = "Unique id assigned by the service.", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Full name of the student.", example = "Sherlock Holmes",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Declared major.", example = "Computer Science",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String major;

    @Schema(description = "Grade point average on a 4.0 scale.", example = "3.75",
            requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0.0", maximum = "4.0")
    private Double gpa;

    @ArraySchema(
            arraySchema = @Schema(description = "Names of the courses this student is enrolled in, "
                    + "with one entry per course.",
                    requiredMode = Schema.RequiredMode.REQUIRED),
            schema = @Schema(example = "CS1"))
    private Set<String> courses;

    @Schema(hidden = true)
    private LocalDate dateOfBirth;

    public StudentResponse() {
    }

    public StudentResponse(Long id, String name, String major, Double gpa, Set<String> courses) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.gpa = gpa;
        this.courses = courses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
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

    public Set<String> getCourses() {
        return courses;
    }

    public void setCourses(Set<String> courses) {
        this.courses = courses;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
