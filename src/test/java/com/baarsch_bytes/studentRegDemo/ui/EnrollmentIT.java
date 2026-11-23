package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.CourseListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.ApiSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Rosters, enrolled counts and capacity: TC E-01 to E-08. */
class EnrollmentIT extends UiTestBase {

    private final ApiSetup api = new ApiSetup(BASE_URL);

    private CourseListPage openPage() {
        return new CourseListPage(driver, wait, BASE_URL).open();
    }

    @Test
    @DisplayName("E-01 Adding a student puts them on that course's roster only, and the count goes up")
    void addStudentToTheRightCourse() {
        String studentName = unique("Enrollee");
        long studentId = api.createStudent(studentName, "Math", 3.0);
        long decoyId = api.createCourse(unique("Decoy Course"), 1, 5, "Room 1");   // created first, so the target is not the first row
        long targetId = api.createCourse(unique("Target Course"), 1, 5, "Room 2");
        CourseListPage page = openPage();

        page.addStudentToCourse(targetId, studentId);

        page.waitForEnrolledCount(targetId, "1");
        assertEquals(Set.of(studentName), page.rosterNamesOf(targetId));
        assertEquals("0", page.enrolledCountOf(decoyId));
        assertEquals(Set.of(), page.rosterNamesOf(decoyId));
        page.open();
        assertEquals("1", page.enrolledCountOf(targetId), "the enrolment must be saved on the server");
        assertEquals(Set.of(studentName), page.rosterNamesOf(targetId));
        assertEquals("0", page.enrolledCountOf(decoyId));
    }

    @Test
    @DisplayName("E-02 Clicking Add Student with nobody selected shows a message and changes nothing")
    void addWithoutSelectionIsRefused() {
        long courseId = api.createCourse(unique("No Selection"), 1, 5, "Room 3");
        CourseListPage page = openPage();

        page.clickAddStudent(courseId);
        String message = page.acceptAlert();

        assertTrue(message.toLowerCase().contains("select a student"),
                "the user should be asked to select a student, but the message was: " + message);
        assertEquals("0", page.enrolledCountOf(courseId));
        assertEquals(Set.of(), page.rosterNamesOf(courseId));
    }

    @Test
    @DisplayName("E-03 Removing a student takes them off the roster and the count goes down")
    void removeStudentFromCourse() {
        String studentName = unique("Leaver");
        long studentId = api.createStudent(studentName, "Math", 3.0);
        long courseId = api.createCourse(unique("Leaving Course"), 1, 5, "Room 4");
        api.enroll(courseId, studentId);
        CourseListPage page = openPage();
        assertEquals("1", page.enrolledCountOf(courseId), "precondition: the student starts enrolled");

        page.clickEdit(courseId);
        page.removeStudentFromCourse(courseId, studentId);

        page.waitForEnrolledCount(courseId, "0");
        assertEquals(Set.of(), page.rosterNamesOf(courseId));
        page.open();
        assertEquals("0", page.enrolledCountOf(courseId), "the removal must be saved on the server");
        assertEquals(Set.of(), page.rosterNamesOf(courseId));
    }

    @Test
    @DisplayName("E-04 A course can be filled exactly to its max size (2 of 2) and shows both students")
    void fillCourseToExactlyMaxSize() {
        String firstName = unique("First Seat");
        String secondName = unique("Second Seat");
        long firstId = api.createStudent(firstName, "Math", 3.0);
        long secondId = api.createStudent(secondName, "Math", 3.1);
        long courseId = api.createCourse(unique("Two Seats"), 1, 2, "Room 5");
        CourseListPage page = openPage();

        page.addStudentToCourse(courseId, firstId);
        page.waitForEnrolledCount(courseId, "1");
        page.waitForAddStudentToFinish(courseId);
        page.addStudentToCourse(courseId, secondId);

        page.waitForEnrolledCount(courseId, "2");
        assertEquals(Set.of(firstName, secondName), page.rosterNamesOf(courseId));
        page.open();
        assertEquals("2", page.enrolledCountOf(courseId));
        assertEquals(Set.of(firstName, secondName), page.rosterNamesOf(courseId));
    }

    @Test
    @DisplayName("E-05 Adding a student beyond the max size is refused and the roster is unchanged")
    void addBeyondMaxSizeIsRefused() {
        String insideName = unique("Inside");
        long insideId = api.createStudent(insideName, "Math", 3.0);
        long outsideId = api.createStudent(unique("Outside"), "Math", 3.0);
        long courseId = api.createCourse(unique("Full Course"), 1, 1, "Room 6");
        api.enroll(courseId, insideId);
        CourseListPage page = openPage();
        page.trackRequests();

        page.addStudentToCourse(courseId, outsideId);
        page.waitForRequestsDone(1);
        page.open();

        assertEquals("1", page.enrolledCountOf(courseId), "a course with max size 1 must not hold 2 students");
        assertEquals(Set.of(insideName), page.rosterNamesOf(courseId));
    }

    @Test
    @Tag("defect")
    @DisplayName("E-06 When a student cannot be added because the course is full, the user is told")
    void fullCourseTellsUser() {
        long insideId = api.createStudent(unique("Inside Told"), "Math", 3.0);
        long outsideId = api.createStudent(unique("Outside Told"), "Math", 3.0);
        long courseId = api.createCourse(unique("Full Told"), 1, 1, "Room 7");
        api.enroll(courseId, insideId);
        CourseListPage page = openPage();

        page.addStudentToCourse(courseId, outsideId);

        assertTrue(page.userIsToldOfFailure(), "the user must be shown that the course is full");
    }

    @Test
    @DisplayName("E-07 Lowering the max size below the number of enrolled students is refused")
    void maxSizeCannotDropBelowEnrolled() {
        String firstName = unique("Seat One");
        long firstId = api.createStudent(firstName, "Math", 3.0);
        long secondId = api.createStudent(unique("Seat Two"), "Math", 3.0);
        long courseId = api.createCourse(unique("Shrinking Course"), 1, 2, "Room 8");
        api.enroll(courseId, firstId);
        api.enroll(courseId, secondId);
        CourseListPage page = openPage();
        page.trackRequests();

        page.clickEdit(courseId);
        page.setEditMaxSize("1");
        page.saveEdit();
        page.waitForRequestsDone(1);
        page.open();

        assertEquals("2", page.maxSizeOf(courseId), "max size must stay at 2 while 2 students are enrolled");
        assertEquals("2", page.enrolledCountOf(courseId));
    }

    @Test
    @DisplayName("E-08 Removing a student from one course leaves their other course untouched")
    void removeFromOneCourseKeepsTheOther() {
        String studentName = unique("Two Courses");
        long studentId = api.createStudent(studentName, "Math", 3.0);
        long firstCourse = api.createCourse(unique("Course One"), 1, 5, "Room 11");
        long secondCourse = api.createCourse(unique("Course Two"), 1, 5, "Room 12");
        api.enroll(firstCourse, studentId);
        api.enroll(secondCourse, studentId);
        CourseListPage page = openPage();

        page.clickEdit(firstCourse);
        page.removeStudentFromCourse(firstCourse, studentId);

        page.waitForEnrolledCount(firstCourse, "0");
        assertEquals("1", page.enrolledCountOf(secondCourse));
        assertEquals(Set.of(studentName), page.rosterNamesOf(secondCourse));
        page.open();
        assertEquals("0", page.enrolledCountOf(firstCourse));
        assertEquals("1", page.enrolledCountOf(secondCourse));
        assertEquals(Set.of(studentName), page.rosterNamesOf(secondCourse));
    }
}
