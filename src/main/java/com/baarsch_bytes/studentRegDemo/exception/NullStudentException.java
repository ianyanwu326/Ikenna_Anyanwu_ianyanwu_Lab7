package com.baarsch_bytes.studentRegDemo.exception;

public class NullStudentException extends RuntimeException {
    public NullStudentException() {
        super("No such student exists.");
    }
}
