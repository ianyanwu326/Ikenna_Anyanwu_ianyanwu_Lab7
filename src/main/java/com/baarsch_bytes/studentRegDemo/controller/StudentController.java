package com.baarsch_bytes.studentRegDemo.controller;

import com.baarsch_bytes.studentRegDemo.dto.CourseResponse;
import com.baarsch_bytes.studentRegDemo.dto.ErrorResponse;
import com.baarsch_bytes.studentRegDemo.dto.StudentRequest;
import com.baarsch_bytes.studentRegDemo.dto.StudentResponse;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.service.StudentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // GET all students
    @GetMapping
    @Operation(operationId = "listStudents", summary = "List students",
            description = "Returns every student, optionally limited to a single major.")
    @ApiResponse(responseCode = "200", description = "The matching students. The list is empty when nothing matches.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudentResponse.class))))
    public List<StudentResponse> getAll(
            @Parameter(description = "Return only students with exactly this major.", example = "Computer Science")
            @RequestParam(required = false) String major) {
        return service.findAll(major);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getStudent", summary = "Get a student")
    @ApiResponse(responseCode = "200", description = "The student.",
            content = @Content(schema = @Schema(implementation = StudentResponse.class)))
    @ApiResponse(responseCode = "404", description = "No student has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public StudentResponse getStudent(
            @Parameter(description = "Id of the student.", example = "1") @PathVariable long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/courses")
    @Operation(operationId = "listStudentCourses", summary = "List a student's courses",
            description = "Returns the full details of each course the student is enrolled in.")
    @ApiResponse(responseCode = "200", description = "The student's courses.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseResponse.class))))
    @ApiResponse(responseCode = "404", description = "No student has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<CourseResponse> getStudentCourses(
            @Parameter(description = "Id of the student.", example = "1") @PathVariable long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet");
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Hidden
    public String export() {
        return service.exportCsv();
    }

    // POST a new student (The target for Postman testing)
    @PostMapping
    @Operation(operationId = "createStudent", summary = "Create a student",
            description = "Creates a student and places them on the roster of any courses listed in `courses`.")
    @ApiResponse(responseCode = "201", description = "The student was created.",
            headers = @Header(name = "Location", description = "URL of the new student.",
                    schema = @Schema(type = "string")),
            content = @Content(schema = @Schema(implementation = StudentResponse.class)))
    @ApiResponse(responseCode = "400", description = "The request failed validation.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody StudentRequest studentRequest) {
        Student student = service.create(studentRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", student.getName() + " added successfully");
        response.put("id", student.getId());
        return ResponseEntity.ok(response);
    }

    // Update
    @PutMapping("/{id}")
    @Operation(operationId = "updateStudent", summary = "Update a student",
            description = "Replaces the student's details and, when `courses` is supplied, their course enrollments.")
    @ApiResponse(responseCode = "200", description = "The updated student.",
            content = @Content(schema = @Schema(implementation = StudentResponse.class)))
    @ApiResponse(responseCode = "400", description = "The request failed validation.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "No student has this id.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Map<String, Object>> update(
            @Valid @RequestBody StudentRequest studentRequest,
            @Parameter(description = "Id of the student.", example = "1") @PathVariable Long id) {
        Student student = service.update(id, studentRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", student.getName() + " updated successfully");
        return ResponseEntity.ok(response);
    }

    // DELETE a student
    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteStudent", summary = "Delete a student",
            description = "Deletes the student and removes them from the roster of every course they are enrolled in.")
    @ApiResponse(responseCode = "204", description = "The student was deleted.", content = @Content)
    @ApiResponse(responseCode = "404", description = "No student has this id.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the student.", example = "1") @PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
