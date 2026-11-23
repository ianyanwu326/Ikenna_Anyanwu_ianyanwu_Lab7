package com.baarsch_bytes.studentRegDemo.ui.support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates test data through the REST API so each UI test can arrange its own data quickly
 * (the simulated latency lives in the browser, so these calls are instant). The behaviour
 * being tested is always driven and checked through the UI, never through this class.
 */
public final class ApiSetup {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    private final String baseUrl;

    public ApiSetup(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Creates a student and returns its database id. */
    public long createStudent(String name, String major, double gpa) {
        String body = "{\"name\":" + json(name) + ",\"major\":" + json(major) + ",\"gpa\":" + gpa + "}";
        return idFrom(post("/api/students", body));
    }

    /** Creates a course with an empty roster and returns its database id. */
    public long createCourse(String name, long instructor, int maxSize, String room) {
        String body = "{\"name\":" + json(name) + ",\"instructor\":" + instructor
                + ",\"maxSize\":" + maxSize + ",\"room\":" + json(room) + "}";
        return idFrom(post("/api/courses", body));
    }

    /** Puts a student on a course roster (used to arrange "already enrolled" situations). */
    public void enroll(long courseId, long studentId) {
        put("/api/courses/addStudent/" + courseId, Long.toString(studentId));
    }

    private String post(String path, String body) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    private String put(String path, String body) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Test data setup failed: HTTP " + response.statusCode()
                        + " for " + request.uri() + ": " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new IllegalStateException("Test data setup failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static long idFrom(String responseBody) {
        Matcher m = ID.matcher(responseBody);
        if (!m.find()) {
            throw new IllegalStateException("No id in response: " + responseBody);
        }
        return Long.parseLong(m.group(1));
    }

    private static String json(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
