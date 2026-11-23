package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.StudentListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.ApiSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Editing and deleting students: TC S-11 to S-18. Each test creates its own students through the API. */
class StudentEditDeleteIT extends UiTestBase {

    private final ApiSetup api = new ApiSetup(BASE_URL);

    private StudentListPage openPage() {
        return new StudentListPage(driver, wait, BASE_URL).open();
    }

    @Test
    @DisplayName("S-11 The edit form is pre-filled with the student's current values")
    void editFormIsPrefilled() {
        String name = unique("Prefill");
        long id = api.createStudent(name, "History", 3.25);
        StudentListPage page = openPage();

        page.clickEdit(id);

        assertEquals(name, page.editNameValue());
        assertEquals("History", page.editMajorValue());
        assertEquals("3.25", page.editGpaValue());
    }

    @Test
    @DisplayName("S-12 Saving an edit updates that student's row and leaves other students alone")
    void saveEditUpdatesOnlyThatStudent() {
        String otherName = unique("Bystander");
        long otherId = api.createStudent(otherName, "Art", 2.5);
        long id = api.createStudent(unique("Before"), "History", 3.0);
        String newName = unique("After");
        StudentListPage page = openPage();

        page.clickEdit(id);
        page.setEditName(newName);
        page.setEditMajor("Philosophy");
        page.setEditGpa("3.7");
        page.saveEdit();
        page.waitForEditFormToClose();

        page.waitForNameOf(id, newName);
        page.waitForMajorOf(id, "Philosophy");
        page.waitForGpaOf(id, "3.7");
        assertEquals(otherName, page.nameOf(otherId));
        assertEquals("Art", page.majorOf(otherId));
        assertEquals("2.5", page.gpaOf(otherId));
        page.open();
        assertEquals(newName, page.nameOf(id), "the edit must be saved on the server, not only on screen");
        assertEquals("Philosophy", page.majorOf(id));
        assertEquals("3.7", page.gpaOf(id));
    }

    @Test
    @DisplayName("S-13 Cancelling an edit changes nothing, even after the fields were changed")
    void cancelEditChangesNothing() {
        String name = unique("Keep Me");
        long id = api.createStudent(name, "Geology", 3.3);
        StudentListPage page = openPage();

        page.clickEdit(id);
        page.setEditName(unique("Changed"));
        page.setEditMajor("Changed Major");
        page.setEditGpa("1.1");
        page.cancelEdit();
        page.waitForEditFormToClose();

        assertEquals(name, page.nameOf(id));
        assertEquals("Geology", page.majorOf(id));
        assertEquals("3.3", page.gpaOf(id));
        page.open();
        assertEquals(name, page.nameOf(id), "nothing may have been saved on the server");
        assertEquals("Geology", page.majorOf(id));
        assertEquals("3.3", page.gpaOf(id));
    }

    @Test
    @DisplayName("S-14 Editing the GPA to 4.01 is rejected and the stored values stay as they were")
    void invalidEditLeavesStoredValuesUnchanged() {
        String name = unique("Stable");
        long id = api.createStudent(name, "Music", 3.6);
        StudentListPage page = openPage();
        page.trackRequests();

        page.clickEdit(id);
        page.setEditGpa("4.01");
        page.saveEdit();
        page.waitForRequestsDone(1);
        page.open();

        assertEquals(name, page.nameOf(id));
        assertEquals("Music", page.majorOf(id));
        assertEquals("3.6", page.gpaOf(id), "the invalid GPA must not have been stored");
    }

    @Test
    @Tag("defect")
    @DisplayName("S-15 When an edit is rejected the user is told and the typed values are kept")
    void rejectedEditTellsUserAndKeepsInput() {
        long id = api.createStudent(unique("Edit Rejected"), "Music", 3.6);
        StudentListPage page = openPage();

        page.clickEdit(id);
        page.setEditGpa("4.01");
        page.saveEdit();
        page.waitForEditToSettle();

        assertTrue(page.userIsToldOfFailure(), "the user must be shown why the edit was not saved");
        assertTrue(page.editFormIsOpen(), "the edit form must stay open so the input is not lost");
        assertEquals("4.01", page.editGpaValue(), "the GPA the user typed must not be lost");
    }

    @Test
    @Tag("defect")
    @DisplayName("S-16 A student with GPA 0 has 0 in the edit form, not an empty field")
    void editFormShowsGpaZero() {
        long id = api.createStudent(unique("Zero Edit"), "Theatre", 0.0);
        StudentListPage page = openPage();

        page.clickEdit(id);

        assertEquals("0", page.editGpaValue(), "GPA 0 is a real value and must not be shown as blank");
    }

    @Test
    @DisplayName("S-17 Deleting a student removes that row only, and it stays gone after a reload")
    void deleteRemovesOnlyThatStudent() {
        String keepName = unique("Keep");
        long keepId = api.createStudent(keepName, "Law", 3.1);   // created first, so the target is never the first row
        long targetId = api.createStudent(unique("Delete Me"), "Law", 3.2);
        StudentListPage page = openPage();

        page.clickDelete(targetId);
        page.waitUntilRowIsGone(targetId);

        assertTrue(page.hasRow(keepId), "the other student must not be deleted");
        assertEquals(keepName, page.nameOf(keepId));
        page.open();
        assertFalse(page.hasRow(targetId), "the student must be gone on the server too");
        assertTrue(page.hasRow(keepId));
    }

    @Test
    @Tag("defect")
    @DisplayName("S-18 A student who is enrolled in a course can be deleted, and stays deleted after a reload")
    void deleteEnrolledStudent() {
        long studentId = api.createStudent(unique("Enrolled Leaver"), "Law", 3.0);
        long courseId = api.createCourse(unique("Contracts"), 1, 5, "Room 1");
        api.enroll(courseId, studentId);
        StudentListPage page = openPage();

        page.clickDelete(studentId);
        page.waitUntilRowIsGone(studentId);
        page.open();

        assertFalse(page.hasRow(studentId), "the row came back, so the server did not really delete the student");
    }
}
