package com.baarsch_bytes.studentRegDemo.exception;

public class NullCourseException extends RuntimeException {
    public NullCourseException() {
        super("No such course exists");
    }
}
