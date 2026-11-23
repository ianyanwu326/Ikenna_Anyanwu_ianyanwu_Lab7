package com.baarsch_bytes.studentRegDemo.service;

import com.baarsch_bytes.studentRegDemo.dto.StudentRequest;
import com.baarsch_bytes.studentRegDemo.dto.StudentResponse;
import com.baarsch_bytes.studentRegDemo.exception.NullStudentException;
import com.baarsch_bytes.studentRegDemo.model.Course;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.repository.CourseRepository;
import com.baarsch_bytes.studentRegDemo.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

// Holds the student business logic so that the controller only deals with HTTP.
@Service
public class StudentService {

    private final StudentRepository repository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository repository, CourseRepository courseRepository) {
        this.repository = repository;
        this.courseRepository = courseRepository;
    }

    public List<StudentResponse> findAll(String major) {
        List<Student> students = (major == null) ? repository.findAll() : repository.findByMajor(major);
        return students.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public StudentResponse findById(long id) {
        Student student = repository.findById(id).orElse(new Student());
        return toResponse(student);
    }

    public Student create(StudentRequest request) {
        return repository.save(applyRequest(request, new Student()));
    }

    public Student update(long id, StudentRequest request) {
        Student student = repository.findById(id).orElseThrow(NullStudentException::new);
        return repository.save(applyRequest(request, student));
    }

    public boolean delete(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public String exportCsv() {
        StringBuilder csv = new StringBuilder("id,name,major,gpa,dateOfBirth\n");
        for (Student student : repository.findAll()) {
            csv.append(student.getId()).append(',')
                    .append(student.getName()).append(',')
                    .append(student.getMajor()).append(',')
                    .append(student.getGpa()).append(',')
                    .append(student.getDateOfBirth()).append('\n');
        }
        return csv.toString();
    }

    // translate between the dto from the client and the entity ruled by the DB.
    private Student applyRequest(StudentRequest request, Student student) {
        student.setName(request.getName());
        student.setMajor(request.getMajor());
        student.setGpa(request.getGpa());
        student.setDateOfBirth(request.getDateOfBirth());
        if (request.getCourses() != null) {
            List<Course> coursesFromDb = courseRepository.findAllById(request.getCourses());
            student.setCourses(new HashSet<>(coursesFromDb));
        }
        return student;
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setMajor(student.getMajor());
        response.setGpa(student.getGpa());
        response.setDateOfBirth(student.getDateOfBirth());
        response.setCourses(
                student.getCourses()
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(Course::getName)
                        .collect(Collectors.toSet()));
        return response;
    }
}
