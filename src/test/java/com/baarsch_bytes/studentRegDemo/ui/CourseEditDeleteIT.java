package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.CourseListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.ApiSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Editing and deleting courses: TC C-08 to C-11. */
class CourseEditDeleteIT extends UiTestBase {

    private final ApiSetup api = new ApiSetup(BASE_URL);

    private CourseListPage openPage() {
        return new CourseListPage(driver, wait, BASE_URL).open();
    }

    @Test
    @DisplayName("C-08 The edit form is pre-filled with the course's current values")
    void editFormIsPrefilled() {
        String name = unique("Prefill Course");
        long id = api.createCourse(name, 5, 12, "Room 9");
        CourseListPage page = openPage();

        page.clickEdit(id);

        assertEquals(name, page.editNameValue());
        assertEquals("5", page.editInstructorValue());
        assertEquals("12", page.editMaxSizeValue());
        assertEquals("Room 9", page.editRoomValue());
    }

    @Test
    @DisplayName("C-09 Saving an edit updates all four fields of that course and leaves other courses alone")
    void saveEditUpdatesOnlyThatCourse() {
        String otherName = unique("Bystander Course");
        long otherId = api.createCourse(otherName, 4, 20, "Room 4");
        long id = api.createCourse(unique("Before Course"), 5, 12, "Room 9");
        String newName = unique("After Course");
        CourseListPage page = openPage();

        page.clickEdit(id);
        page.setEditName(newName);
        page.setEditInstructor("8");
        page.setEditMaxSize("15");
        page.setEditRoom("Room 10");
        page.saveEdit();
        page.waitForEditFormToClose();

        page.waitForNameOf(id, newName);
        page.waitForMaxSizeOf(id, "15");
        assertEquals("8", page.instructorOf(id));
        assertEquals("Room 10", page.roomOf(id));
        assertEquals(otherName, page.nameOf(otherId));
        assertEquals("4", page.instructorOf(otherId));
        assertEquals("20", page.maxSizeOf(otherId));
        assertEquals("Room 4", page.roomOf(otherId));
        page.open();
        assertEquals(newName, page.nameOf(id), "the edit must be saved on the server, not only on screen");
        assertEquals("8", page.instructorOf(id));
        assertEquals("15", page.maxSizeOf(id));
        assertEquals("Room 10", page.roomOf(id));
    }

    @Test
    @DisplayName("C-10 Cancelling an edit changes nothing, even after the fields were changed")
    void cancelEditChangesNothing() {
        String name = unique("Keep Course");
        long id = api.createCourse(name, 6, 18, "Room 6");
        CourseListPage page = openPage();

        page.clickEdit(id);
        page.setEditName(unique("Changed Course"));
        page.setEditInstructor("99");
        page.setEditMaxSize("99");
        page.setEditRoom("Changed Room");
        page.cancelEdit();
        page.waitForEditFormToClose();

        assertEquals(name, page.nameOf(id));
        assertEquals("6", page.instructorOf(id));
        assertEquals("18", page.maxSizeOf(id));
        assertEquals("Room 6", page.roomOf(id));
        page.open();
        assertEquals(name, page.nameOf(id), "nothing may have been saved on the server");
        assertEquals("6", page.instructorOf(id));
        assertEquals("18", page.maxSizeOf(id));
        assertEquals("Room 6", page.roomOf(id));
    }

    @Test
    @DisplayName("C-11 Deleting a course removes that row only, and it stays gone after a reload")
    void deleteRemovesOnlyThatCourse() {
        String keepName = unique("Keep Course");
        long keepId = api.createCourse(keepName, 1, 10, "Room 1");
        long targetId = api.createCourse(unique("Delete Course"), 1, 10, "Room 2");
        CourseListPage page = openPage();

        page.clickDelete(targetId);
        page.waitUntilRowIsGone(targetId);

        assertTrue(page.hasRow(keepId), "the other course must not be deleted");
        assertEquals(keepName, page.nameOf(keepId));
        page.open();
        assertFalse(page.hasRow(targetId), "the course must be gone on the server too");
        assertTrue(page.hasRow(keepId));
    }
}
