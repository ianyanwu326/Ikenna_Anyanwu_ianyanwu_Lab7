package com.baarsch_bytes.studentRegDemo.ui.support;

/** Small helpers for building boundary-value test data. */
public final class TestData {
    private TestData() {}

    /** Pads (with the letter 'x') or returns the text so that it is exactly {@code length} characters long. */
    public static String exactLength(String uniquePrefix, int length) {
        if (uniquePrefix.length() > length) {
            throw new IllegalArgumentException("prefix longer than " + length);
        }
        return uniquePrefix + "x".repeat(length - uniquePrefix.length());
    }
}
