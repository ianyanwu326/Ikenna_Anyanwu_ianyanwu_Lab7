package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.StudentListPage;
import com.baarsch_bytes.studentRegDemo.ui.support.ApiSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Part 1: reliable rewrites of the four tests in AntiPatternExamplesIT.
 * Each test waits for the page instead of sleeping, finds rows by what they contain,
 * creates its own data, and checks the values the user would see.
 */
class AntiPatternFixedIT extends UiTestBase {

    private final ApiSetup api = new ApiSetup(BASE_URL);

    /** Fixes createStudent: no sleeps, a unique name, and all three values checked. */
    @Test
    @DisplayName("Fixed createStudent: the new row shows the name, major and GPA that were typed")
    void createStudent() {
        StudentListPage page = new StudentListPage(driver, wait, BASE_URL).open();
        String name = unique("Grace Hopper");

        page.submitNewStudent(name, "Computer Science", "3.8");
        long id = page.waitForStudentNamed(name);

        assertEquals(name, page.nameOf(id));
        assertEquals("Computer Science", page.majorOf(id));
        assertEquals("3.8", page.gpaOf(id));
        assertEquals(1, page.countRowsNamed(name), "the student must be listed exactly once");
    }

    /** Fixes editStudent: creates its own student, edits that row, and checks the saved result after a reload. */
    @Test
    @DisplayName("Fixed editStudent: the new major is saved, not just typed")
    void editStudent() {
        String name = unique("Edit Target");
        long id = api.createStudent(name, "Computer Science", 3.8);
        StudentListPage page = new StudentListPage(driver, wait, BASE_URL).open();

        page.clickEdit(id);
        page.setEditMajor("Mathematics");
        page.saveEdit();
        page.waitForEditFormToClose();
        page.waitForMajorOf(id, "Mathematics");

        page.open();
        assertEquals("Mathematics", page.majorOf(id), "the change must survive a reload");
        assertEquals(name, page.nameOf(id), "the name must not have changed");
        assertEquals("3.8", page.gpaOf(id), "the GPA must not have changed");
    }

    /** Fixes studentTableHasData: waits for the table, finds the cell by id, and checks its content. */
    @Test
    @DisplayName("Fixed studentTableHasData: the table lists a known student with the right GPA")
    void studentTableHasData() {
        String name = unique("Table Data");
        long id = api.createStudent(name, "Physics", 3.4);
        StudentListPage page = new StudentListPage(driver, wait, BASE_URL).open();

        page.waitForRow(id);

        assertEquals(name, page.nameOf(id));
        assertEquals("3.4", page.gpaOf(id));
    }

    /** Fixes deleteStudent: deletes a chosen row, lets failures fail, and checks it is gone for good. */
    @Test
    @DisplayName("Fixed deleteStudent: the chosen student is removed and the others stay")
    void deleteStudent() {
        long keepId = api.createStudent(unique("Keep"), "Art", 3.0);
        long deleteId = api.createStudent(unique("Delete"), "Art", 3.0);
        StudentListPage page = new StudentListPage(driver, wait, BASE_URL).open();

        page.clickDelete(deleteId);
        page.waitUntilRowIsGone(deleteId);

        assertTrue(page.hasRow(keepId), "deleting one student must not delete another");
        page.open();
        assertFalse(page.hasRow(deleteId), "the student must be gone after a reload");
    }
}
