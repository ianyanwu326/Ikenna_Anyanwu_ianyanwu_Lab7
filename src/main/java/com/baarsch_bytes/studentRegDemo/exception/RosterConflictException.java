package com.baarsch_bytes.studentRegDemo.exception;

// Thrown when a roster change conflicts with the current state of the course.
public class RosterConflictException extends RuntimeException {
    public RosterConflictException(String message) {
        super(message);
    }
}
