package com.baarsch_bytes.studentRegDemo.controller;

import com.baarsch_bytes.studentRegDemo.dto.CourseRequest;
import com.baarsch_bytes.studentRegDemo.dto.CourseResponse;
import com.baarsch_bytes.studentRegDemo.dto.ErrorResponse;
import com.baarsch_bytes.studentRegDemo.dto.RosterChangeRequest;
import com.baarsch_bytes.studentRegDemo.exception.NullCourseException;
import com.baarsch_bytes.studentRegDemo.exception.NullStudentException;
import com.baarsch_bytes.studentRegDemo.exception.RosterConflictException;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    @Tag(name = "Courses")
    @Operation(operationId = "listCourses", summary = "List courses",
            description = "Returns every course, optionally limited to a single instructor.",
            parameters = @Parameter(name = "instructorId", in = ParameterIn.QUERY,
                    description = "Return only courses taught by this instructor.", example = "2",
                    schema = @Schema(type = "integer", format = "int64")))
    @ApiResponse(responseCode = "200", description = "The matching courses. The list is empty when nothing matches.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseResponse.class))))
    public List<CourseResponse> getCourses(
            @Parameter(hidden = true) @RequestParam(required = false) Long instructor) {
        return service.findAll(instructor);
    }

    @GetMapping("/{id}")
    @Tag(name = "Courses")
    @Operation(operationId = "getCourse", summary = "Get a course")
    @ApiResponse(responseCode = "200", description = "The course.",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @ApiResponse(responseCode = "404", description = "No course has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CourseResponse getCourse(
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Tag(name = "Courses")
    @Operation(operationId = "createCourse", summary = "Create a course",
            description = "Creates a course, optionally with an initial roster.")
    @ApiResponse(responseCode = "201", description = "The course was created.",
            headers = @Header(name = "Location", description = "URL of the new course.",
                    schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @ApiResponse(responseCode = "400", description = "The request failed validation, "
            + "or the initial roster is larger than `maxSize`.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "`roster` contains an id that does not belong to a student.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Tag(name = "Courses")
    @Operation(operationId = "updateCourse", summary = "Update a course",
            description = "Replaces the course's details and, when `roster` is supplied, its roster.")
    @ApiResponse(responseCode = "200", description = "The updated course.",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @ApiResponse(responseCode = "400", description = "The request failed validation, "
            + "or `maxSize` is below the size of the roster.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No course has this id, "
            + "or `roster` contains an id that does not belong to a student.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CourseResponse update(
            @Valid @RequestBody CourseRequest request,
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long id) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Tag(name = "Courses")
    @Operation(operationId = "deleteCourse", summary = "Delete a course",
            description = "Deletes the course. The students on its roster are not deleted.")
    @ApiResponse(responseCode = "204", description = "The course was deleted.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No course has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/addStudent/{courseId}")
    @Tag(name = "Rosters")
    @Operation(operationId = "addStudentToCourse", summary = "Add a student to a course",
            description = "Adds the student to the roster. The body is the student's id as a bare JSON number. "
                    + "A course can never hold more than `maxSize` students.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    description = "Id of the student.",
                    content = @Content(schema = @Schema(type = "integer", format = "int64", example = "1"))))
    @ApiResponse(responseCode = "200", description = "The course with its updated roster.",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @ApiResponse(responseCode = "404", description = "No course or no student has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "The course is full, or the student is already on the roster.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<String> addStudent(
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long courseId,
            @RequestBody long studentId) {
        try {
            Student student = service.addStudent(courseId, studentId);
            return ResponseEntity.ok(student.getName() + " added successfully");
        } catch (NullCourseException | NullStudentException | RosterConflictException e) {
            // badRequest returns a Body Builder--this is used to create a Response entity.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/removeStudent/{courseId}")
    @Tag(name = "Rosters")
    @Operation(operationId = "removeStudentFromCourse", summary = "Remove a student from a course",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = RosterChangeRequest.class))))
    @ApiResponse(responseCode = "200", description = "The course with its updated roster.",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @ApiResponse(responseCode = "404", description = "No course or no student has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "The student is not on the roster.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CourseResponse removeStudent(
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long courseId,
            @RequestBody long studentId) {
        return service.removeStudent(courseId, studentId);
    }

    @GetMapping("/getEnrollment/{courseId}")
    @Tag(name = "Rosters")
    @Operation(operationId = "getCourseEnrollment", summary = "Get the number of students in a course",
            description = "Returns the number of students currently on the roster.")
    @ApiResponse(responseCode = "200", description = "The number of students on the roster.",
            content = @Content(schema = @Schema(type = "integer", format = "int64", minimum = "0", example = "12")))
    @ApiResponse(responseCode = "404", description = "No course has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Long> getEnrollment(
            @Parameter(description = "Id of the course.", example = "1") @PathVariable Long courseId) {
        try {
            return ResponseEntity.ok(service.getEnrollment(courseId));
        } catch (NullCourseException e) {
            // Going to say that if the course cannot be found, we will return -1.
            //It might confuse people--but we'll throw it somewhere in the docs.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1L);
        }
    }
}
