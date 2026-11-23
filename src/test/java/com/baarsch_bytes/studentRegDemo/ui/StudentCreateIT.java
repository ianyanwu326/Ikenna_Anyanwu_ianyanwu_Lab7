package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.StudentListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Creating students: TC S-01 to S-10 (see the test case document). */
class StudentCreateIT extends UiTestBase {

    private StudentListPage openPage() {
        return new StudentListPage(driver, wait, BASE_URL).open();
    }

    /** Submits a student that the app should refuse, waits for the server to answer, reloads, and returns the page. */
    private StudentListPage submitRejectedStudentAndReload(String name, String major, String gpa) {
        StudentListPage page = openPage();
        page.trackRequests();
        page.submitNewStudent(name, major, gpa);
        page.waitForRequestsDone(1);
        return page.open();
    }

    @Test
    @DisplayName("S-01 A new student appears with the name, major and GPA that were entered")
    void createdStudentShowsAllValues() {
        StudentListPage page = openPage();
        String name = unique("Ada Lovelace");

        page.submitNewStudent(name, "Mathematics", "3.5");
        long id = page.waitForStudentNamed(name);

        assertEquals(name, page.nameOf(id));
        assertEquals("Mathematics", page.majorOf(id));
        assertEquals("3.5", page.gpaOf(id));
        page.open();
        assertEquals("Mathematics", page.majorOf(id), "the student must still be there after a reload");
        assertEquals("3.5", page.gpaOf(id));
    }

    @Test
    @DisplayName("S-02 The add form is emptied after a student is added")
    void addFormIsClearedAfterSuccess() {
        StudentListPage page = openPage();
        String name = unique("Grace Hopper");

        page.submitNewStudent(name, "Computer Science", "3.8");
        page.waitForStudentNamed(name);

        assertTrue(page.addFormBecomesEmpty(), "name, major and GPA fields should be empty after a successful add");
    }

    @Test
    @DisplayName("S-03 GPA 0 (lower boundary) is accepted and shown as 0")
    void gpaZeroIsAccepted() {
        StudentListPage page = openPage();
        String name = unique("Zero GPA");

        page.submitNewStudent(name, "Physics", "0");
        long id = page.waitForStudentNamed(name);

        page.waitForGpaOf(id, "0");
        assertEquals("Physics", page.majorOf(id));
    }

    @Test
    @DisplayName("S-04 GPA 4.0 (upper boundary) is accepted and shown as 4")
    void gpaFourIsAccepted() {
        StudentListPage page = openPage();
        String name = unique("Four GPA");

        page.submitNewStudent(name, "Chemistry", "4.0");
        long id = page.waitForStudentNamed(name);

        page.waitForGpaOf(id, "4");
        assertEquals("Chemistry", page.majorOf(id));
    }

    @Test
    @DisplayName("S-05 GPA -0.1 (below the lower boundary) is not saved")
    void negativeGpaIsNotSaved() {
        String name = unique("Negative GPA");
        StudentListPage page = submitRejectedStudentAndReload(name, "Biology", "-0.1");
        assertEquals(0, page.countRowsNamed(name), "a student with GPA -0.1 must not be created");
    }

    @Test
    @DisplayName("S-06 GPA 4.01 (above the upper boundary) is not saved")
    void gpaAboveFourIsNotSaved() {
        String name = unique("High GPA");
        StudentListPage page = submitRejectedStudentAndReload(name, "Biology", "4.01");
        assertEquals(0, page.countRowsNamed(name), "a student with GPA 4.01 must not be created");
    }

    @Test
    @DisplayName("S-07 A name of exactly 255 characters (upper boundary) is accepted in full")
    void name255IsAccepted() {
        StudentListPage page = openPage();
        String name = TestData.exactLength(unique("Long"), 255);

        page.submitNewStudent(name, "Linguistics", "3.0");
        long id = page.waitForStudentNamed(name);

        assertEquals(255, page.nameOf(id).length());
        assertEquals(name, page.nameOf(id));
    }

    @Test
    @DisplayName("S-08 A name of 256 characters (above the boundary) is not saved")
    void name256IsNotSaved() {
        String name = TestData.exactLength(unique("TooLong"), 256);
        StudentListPage page = submitRejectedStudentAndReload(name, "Linguistics", "3.0");
        assertEquals(0, page.countRowsNamed(name), "a 256-character name must not be accepted");
    }

    @Test
    @DisplayName("S-09 A student with a blank name is not saved")
    void blankNameIsNotSaved() {
        StudentListPage page = openPage();
        int rowsBefore = page.rows().size();
        page.trackRequests();

        page.submitNewStudent("", unique("Blank Name Major"), "3.0");
        page.waitForRequestsDone(1);
        page.open();

        assertEquals(rowsBefore, page.rows().size(), "no row may be added for a blank name");
    }

    @Test
    @Tag("defect")
    @DisplayName("S-10 When a create is rejected the user is told and the input is kept")
    void rejectedCreateTellsUserAndKeepsInput() {
        StudentListPage page = openPage();
        String name = unique("Rejected Student");

        page.submitNewStudent(name, "Economics", "4.5");
        page.waitForAddToSettle();

        assertTrue(page.userIsToldOfFailure(), "the user must be shown why the student was not added");
        assertEquals(name, page.newNameValue(), "the name the user typed must not be lost");
        assertEquals("Economics", page.newMajorValue(), "the major the user typed must not be lost");
        assertEquals("4.5", page.newGpaValue(), "the GPA the user typed must not be lost");
    }
}
