package com.baarsch_bytes.studentRegDemo.exception;

import com.baarsch_bytes.studentRegDemo.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

// ResponseEntityExceptionHandler already knows the right status for Spring's own exceptions
// (malformed JSON, wrong method, unknown path, and so on).  We only need to give them a body.
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // This handler will take handle the validation exceptions.
    // It will create a Response entity and add the error messages from
    // the thrown exception to the response and send it back instead of
    // the exception.
    // This will handle all standard Springboot Validation Exceptions--
    // it is not tied to any specific class.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        // create the map that will hold all the errors and messages
        Map<String, String> errors = new HashMap<>();

        // get the errors from the exception and add them to the error map
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        // return an HTTP Error Response with the errors and messages attached
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({NullStudentException.class, NullCourseException.class})
    public ResponseEntity<Object> handleNotFound(RuntimeException e, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(RosterConflictException.class)
    public ResponseEntity<Object> handleRosterConflict(RosterConflictException e, WebRequest request) {
        return error(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    // The catch-all.  The details go to the log rather than to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception e, WebRequest request) {
        log.error("Unhandled exception", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // Every exception Spring handles for us passes through here on its way out.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        String message = (e instanceof org.springframework.web.ErrorResponse springError
                && springError.getBody().getDetail() != null)
                ? springError.getBody().getDetail()
                : httpStatus.getReasonPhrase();
        ErrorResponse errorBody = buildBody(httpStatus, message, request);
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.putAll(headers);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(status).headers(jsonHeaders).body(errorBody);
    }

    private ResponseEntity<Object> error(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildBody(status, message, request));
    }

    private ErrorResponse buildBody(HttpStatus status, String message, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        return new ErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
