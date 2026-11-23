package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.CourseListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Creating courses: TC C-01 to C-07. */
class CourseCreateIT extends UiTestBase {

    private CourseListPage openPage() {
        return new CourseListPage(driver, wait, BASE_URL).open();
    }

    private CourseListPage submitRejectedCourseAndReload(String name, String instructor, String maxSize, String room) {
        CourseListPage page = openPage();
        page.trackRequests();
        page.submitNewCourse(name, instructor, maxSize, room);
        page.waitForRequestsDone(1);
        return page.open();
    }

    @Test
    @DisplayName("C-01 A new course appears with its name, instructor, max size and room, and no students")
    void createdCourseShowsAllValues() {
        CourseListPage page = openPage();
        String name = unique("Algorithms");

        page.submitNewCourse(name, "7", "30", "MCS 101");
        long id = page.waitForCourseNamed(name);

        assertEquals(name, page.nameOf(id));
        assertEquals("7", page.instructorOf(id));
        assertEquals("30", page.maxSizeOf(id));
        assertEquals("MCS 101", page.roomOf(id));
        assertEquals("0", page.enrolledCountOf(id));
        assertEquals(Set.of(), page.rosterNamesOf(id));
        page.open();
        assertEquals("30", page.maxSizeOf(id), "the course must still be there after a reload");
        assertEquals("MCS 101", page.roomOf(id));
    }

    @Test
    @DisplayName("C-02 The add form is emptied after a course is added")
    void addFormIsClearedAfterSuccess() {
        CourseListPage page = openPage();
        String name = unique("Databases");

        page.submitNewCourse(name, "3", "25", "MCS 202");
        page.waitForCourseNamed(name);

        assertTrue(page.addFormBecomesEmpty(), "all four fields should be empty after a successful add");
    }

    @Test
    @DisplayName("C-03 A max size of 1 (lower boundary) is accepted")
    void maxSizeOneIsAccepted() {
        CourseListPage page = openPage();
        String name = unique("Seminar");

        page.submitNewCourse(name, "2", "1", "Room A");
        long id = page.waitForCourseNamed(name);

        assertEquals("1", page.maxSizeOf(id));
    }

    @Test
    @DisplayName("C-04 A max size of 0 (below the lower boundary) is not saved")
    void maxSizeZeroIsNotSaved() {
        String name = unique("Empty Class");
        CourseListPage page = submitRejectedCourseAndReload(name, "2", "0", "Room B");
        assertEquals(0, page.countRowsNamed(name), "a course that holds nobody must not be created");
    }

    @Test
    @DisplayName("C-05 A course name of exactly 255 characters (upper boundary) is accepted in full")
    void courseName255IsAccepted() {
        CourseListPage page = openPage();
        String name = TestData.exactLength(unique("LongCourse"), 255);

        page.submitNewCourse(name, "2", "10", "Room C");
        long id = page.waitForCourseNamed(name);

        assertEquals(name, page.nameOf(id));
    }

    @Test
    @DisplayName("C-06 A course name of 256 characters (above the boundary) is not saved")
    void courseName256IsNotSaved() {
        String name = TestData.exactLength(unique("TooLongCourse"), 256);
        CourseListPage page = submitRejectedCourseAndReload(name, "2", "10", "Room D");
        assertEquals(0, page.countRowsNamed(name), "a 256-character course name must not be accepted");
    }

    @Test
    @Tag("defect")
    @DisplayName("C-07 When a course create is rejected the user is told and the input is kept")
    void rejectedCreateTellsUserAndKeepsInput() {
        CourseListPage page = openPage();
        String name = unique("Rejected Course");

        page.submitNewCourse(name, "2", "0", "Room E");
        page.waitForAddToSettle();

        assertTrue(page.userIsToldOfFailure(), "the user must be shown why the course was not added");
        assertEquals(name, page.newNameValue(), "the name the user typed must not be lost");
        assertEquals("2", page.newInstructorValue());
        assertEquals("0", page.newMaxSizeValue());
        assertEquals("Room E", page.newRoomValue());
    }
}
