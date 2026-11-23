package com.baarsch_bytes.studentRegDemo.service;

import com.baarsch_bytes.studentRegDemo.dto.CourseRequest;
import com.baarsch_bytes.studentRegDemo.dto.CourseResponse;
import com.baarsch_bytes.studentRegDemo.exception.NullCourseException;
import com.baarsch_bytes.studentRegDemo.exception.NullStudentException;
import com.baarsch_bytes.studentRegDemo.exception.RosterConflictException;
import com.baarsch_bytes.studentRegDemo.model.Course;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.repository.CourseRepository;
import com.baarsch_bytes.studentRegDemo.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

// Holds the course and roster business logic so that the controller only deals with HTTP.
@Service
public class CourseService {

    private final CourseRepository repository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository repository, StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
    }

    public List<CourseResponse> findAll(Long instructor) {
        List<Course> courses = (instructor == null)
                ? repository.findAll()
                : repository.findByInstructor(instructor);
        return courses.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CourseResponse findById(long id) {
        return toResponse(findCourse(id));
    }

    public CourseResponse create(CourseRequest request) {
        return toResponse(repository.save(applyRequest(request, new Course())));
    }

    public CourseResponse update(long id, CourseRequest request) {
        return toResponse(repository.save(applyRequest(request, findCourse(id))));
    }

    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new NullCourseException();
        }
        repository.deleteById(id);
    }

    public Student addStudent(long courseId, long studentId) {
        Course course = findCourse(courseId);
        Student student = studentRepository.findById(studentId).orElseThrow(NullStudentException::new);
        if (course.getRoster().size() <= course.getMaxSize()) {
            course.getRoster().add(student);
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            repository.save(course);
            return student;
        }
        throw new RosterConflictException("Course is full");
    }

    public CourseResponse removeStudent(long courseId, long studentId) {
        Course course = findCourse(courseId);
        Student student = studentRepository.findById(studentId).orElseThrow(NullStudentException::new);
        if (!course.getRoster().contains(student)) {
            throw new RosterConflictException("Student not in that course");
        }
        course.getRoster().remove(student);
        course.setEnrolledCount(course.getEnrolledCount() - 1);
        return toResponse(repository.save(course));
    }

    public long getEnrollment(long courseId) {
        return findCourse(courseId).getEnrolledCount();
    }

    private Course findCourse(long id) {
        return repository.findById(id).orElseThrow(NullCourseException::new);
    }

    // translate between the dto from the client and the entity ruled by the DB.
    private Course applyRequest(CourseRequest request, Course course) {
        course.setName(request.getName());
        course.setInstructor(request.getInstructor());
        course.setMaxSize(request.getMaxSize());
        course.setRoom(request.getRoom());
        if (request.getRoster() != null) {
            List<Student> studentsFromDb = studentRepository.findAllById(request.getRoster());
            course.setRoster(new HashSet<>(studentsFromDb));
        }
        return course;
    }

    private CourseResponse toResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setInstructor(course.getInstructor());
        response.setMaxSize(course.getMaxSize());
        response.setRoom(course.getRoom());
        response.setRoster(
                course.getRoster().stream()
                        .map(Student::getName)
                        .collect(Collectors.toSet()));
        response.setStatus(course.getRoster().size() >= course.getMaxSize() ? "FULL" : "OPEN");
        return response;
    }
}
