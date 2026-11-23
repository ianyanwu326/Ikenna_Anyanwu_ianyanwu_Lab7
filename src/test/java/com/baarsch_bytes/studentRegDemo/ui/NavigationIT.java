package com.baarsch_bytes.studentRegDemo.ui;

import com.baarsch_bytes.studentRegDemo.ui.pages.CourseListPage;
import com.baarsch_bytes.studentRegDemo.ui.pages.StudentListPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Navigation and page structure: TC N-01. */
class NavigationIT extends UiTestBase {

    @Test
    @DisplayName("N-01 The nav links switch between the Courses and Students pages, each with the right headings")
    void navLinksSwitchPages() {
        CourseListPage courses = new CourseListPage(driver, wait, BASE_URL).open();
        assertEquals("Available Courses", courses.headingText());
        assertEquals(List.of("Course Name", "Instructor", "Max Size", "Room", "Enrolled Count",
                "Roster", "Delete", "Edit", "Add Student"), courses.columnHeadings());

        courses.goToStudentsViaNav();
        StudentListPage students = new StudentListPage(driver, wait, BASE_URL).waitUntilLoaded();
        assertEquals("Students", students.headingText());
        assertEquals(List.of("Student Name", "Major", "GPA", "Delete", "Edit"), students.columnHeadings());

        students.goToCoursesViaNav();
        courses.waitUntilLoaded();
        assertEquals("Available Courses", courses.headingText());
    }
}
