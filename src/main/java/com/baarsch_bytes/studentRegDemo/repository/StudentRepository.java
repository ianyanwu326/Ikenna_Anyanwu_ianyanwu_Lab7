package com.baarsch_bytes.studentRegDemo.repository;

// import the model that defines the student data type.
import com.baarsch_bytes.studentRegDemo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// uses inheritance to acquire the typical CRUD operations.
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Standard methods like save(), findAll(), and findById() are already here!
    List<Student> findByMajor(String major);
}
