package com.baarsch_bytes.studentRegDemo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Page Object for the Courses page (the home page). Row controls such as Edit, Delete,
 * the student drop-down and the Add Student button repeat the same id on every row, so
 * they are always found through the course's own row.
 */
public class CourseListPage extends BasePage {

    private static final By TABLE = By.id("course-list-table");
    private static final By NEW_NAME = By.id("new-course-name");
    private static final By NEW_INSTRUCTOR = By.id("new-course-instructor");
    private static final By NEW_MAX_SIZE = By.id("new-course-max-size");
    private static final By NEW_ROOM = By.id("new-course-room");
    // The Add Course button has no id, so it is found through its container.
    private static final By ADD_BUTTON = By.cssSelector("#new-course-fields button");

    private static final By EDIT_NAME = By.id("edit-course-name");
    private static final By EDIT_INSTRUCTOR = By.id("edit-course-instructor");
    private static final By EDIT_MAX_SIZE = By.id("edit-course-max-size");
    private static final By EDIT_ROOM = By.id("edit-course-room");
    private static final By EDIT_SAVE = By.id("edit-course-save-button");
    private static final By EDIT_CANCEL = By.id("edit-course-cancel-button");
    private static final By REMOVE_SELECT = By.id("remove-student-select");
    private static final By REMOVE_BUTTON = By.id("remove-student-button");

    public CourseListPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        super(driver, wait, baseUrl);
    }

    public CourseListPage open() {
        driver.get(baseUrl + "/");
        return waitUntilLoaded();
    }

    public CourseListPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        return this;
    }

    // ---- adding a course ---------------------------------------------------------------

    public void submitNewCourse(String name, String instructor, String maxSize, String room) {
        fillNewCourseForm(name, instructor, maxSize, room);
        wait.until(ExpectedConditions.elementToBeClickable(ADD_BUTTON)).click();
    }

    public void fillNewCourseForm(String name, String instructor, String maxSize, String room) {
        type(NEW_NAME, name);
        type(NEW_INSTRUCTOR, instructor);
        type(NEW_MAX_SIZE, maxSize);
        type(NEW_ROOM, room);
    }

    public boolean addFormBecomesEmpty() { return becomesTrue(this::addFormIsEmpty); }

    public boolean addFormIsEmpty() {
        return valueOf(NEW_NAME).isEmpty() && valueOf(NEW_INSTRUCTOR).isEmpty()
                && valueOf(NEW_MAX_SIZE).isEmpty() && valueOf(NEW_ROOM).isEmpty();
    }

    /** Waits until the add has finished: form emptied (success) or an error showing. */
    public void waitForAddToSettle() {
        wait.until(d -> addFormIsEmpty() || errorVisibleNow());
    }

    public String newNameValue() { return valueOf(NEW_NAME); }
    public String newInstructorValue() { return valueOf(NEW_INSTRUCTOR); }
    public String newMaxSizeValue() { return valueOf(NEW_MAX_SIZE); }
    public String newRoomValue() { return valueOf(NEW_ROOM); }

    // ---- reading the table -------------------------------------------------------------

    public long waitForCourseNamed(String name) {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(nameCell(name)));
        return Long.parseLong(cell.getDomAttribute("id").substring("course-name-".length()));
    }

    public int countRowsNamed(String name) {
        return driver.findElements(nameCell(name)).size();
    }

    public boolean hasRow(long id) {
        return !driver.findElements(By.id("course-row-" + id)).isEmpty();
    }

    public void waitForRow(long id) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("course-row-" + id)));
    }

    public void waitUntilRowIsGone(long id) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("course-row-" + id)));
    }

    public String nameOf(long id) { return text("course-name-" + id); }
    public String instructorOf(long id) { return text("course-instructor-" + id); }
    public String maxSizeOf(long id) { return text("course-max-size-" + id); }
    public String roomOf(long id) { return text("course-room-" + id); }

    /** The Enrolled Count column. (Its id is "course-roster-N", which is misleading.) */
    public String enrolledCountOf(long id) { return text("course-roster-" + id); }

    /** The Roster column: the names shown, as a set. The cell has no id, so it is found next to the count cell. */
    public Set<String> rosterNamesOf(long id) {
        String raw = driver.findElement(By.xpath("//td[@id='course-roster-" + id + "']/following-sibling::td[1]"))
                .getText().trim();
        if (raw.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(raw.split("\\s*,\\s*")).collect(Collectors.toSet());
    }

    public void waitForEnrolledCount(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("course-roster-" + id), expected));
    }
    public void waitForNameOf(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("course-name-" + id), expected));
    }
    public void waitForMaxSizeOf(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("course-max-size-" + id), expected));
    }

    public List<String> columnHeadings() {
        return driver.findElements(By.cssSelector("#course-list-table thead th")).stream()
                .map(WebElement::getText).toList();
    }

    // ---- enrolling ---------------------------------------------------------------------

    /** Chooses a student in this course's drop-down (does not click Add). */
    public void selectStudentToAdd(long courseId, long studentId) {
        By option = By.xpath("//tr[@id='course-row-" + courseId + "']//select//option[@value='" + studentId + "']");
        wait.until(ExpectedConditions.presenceOfElementLocated(option));
        new Select(driver.findElement(By.xpath("//tr[@id='course-row-" + courseId + "']//select")))
                .selectByValue(Long.toString(studentId));
    }

    public void clickAddStudent(long courseId) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//tr[@id='course-row-" + courseId + "']//button[normalize-space()='Add Student']"))).click();
    }

    /** Selects the student in this course's row and clicks Add Student. */
    public void addStudentToCourse(long courseId, long studentId) {
        selectStudentToAdd(courseId, studentId);
        clickAddStudent(courseId);
    }

    /**
     * Waits until the page has finished handling a successful Add Student: the drop-down goes
     * back to its "Select a Student" placeholder. Wait for this before the next selection,
     * otherwise the late reset can wipe the new selection.
     */
    public void waitForAddStudentToFinish(long courseId) {
        By select = By.xpath("//tr[@id='course-row-" + courseId + "']//select");
        wait.until(d -> new Select(d.findElement(select)).getFirstSelectedOption()
                .getDomProperty("value").isEmpty());
    }

    // ---- removing from a course (done inside the edit row) -----------------------------

    public void removeStudentFromCourse(long courseId, long studentId) {
        By option = By.xpath("//select[@id='remove-student-select']//option[@value='" + studentId + "']");
        wait.until(ExpectedConditions.presenceOfElementLocated(option));
        new Select(driver.findElement(REMOVE_SELECT)).selectByValue(Long.toString(studentId));
        wait.until(ExpectedConditions.elementToBeClickable(REMOVE_BUTTON)).click();
    }

    // ---- editing -----------------------------------------------------------------------

    public void clickEdit(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(rowButton(id, "Edit"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EDIT_NAME));
    }

    public String editNameValue() { return valueOf(EDIT_NAME); }
    public String editInstructorValue() { return valueOf(EDIT_INSTRUCTOR); }
    public String editMaxSizeValue() { return valueOf(EDIT_MAX_SIZE); }
    public String editRoomValue() { return valueOf(EDIT_ROOM); }

    public void setEditName(String v) { type(EDIT_NAME, v); }
    public void setEditInstructor(String v) { type(EDIT_INSTRUCTOR, v); }
    public void setEditMaxSize(String v) { type(EDIT_MAX_SIZE, v); }
    public void setEditRoom(String v) { type(EDIT_ROOM, v); }

    public void saveEdit() { wait.until(ExpectedConditions.elementToBeClickable(EDIT_SAVE)).click(); }
    public void cancelEdit() { wait.until(ExpectedConditions.elementToBeClickable(EDIT_CANCEL)).click(); }

    public boolean editFormIsOpen() { return !driver.findElements(EDIT_NAME).isEmpty(); }

    public void waitForEditFormToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(EDIT_NAME));
    }

    public void waitForEditToSettle() {
        wait.until(d -> !editFormIsOpen() || errorVisibleNow());
    }

    // ---- deleting ----------------------------------------------------------------------

    public void clickDelete(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(rowButton(id, "Delete"))).click();
    }

    // ---- helpers -----------------------------------------------------------------------

    private String text(String id) { return driver.findElement(By.id(id)).getText(); }

    private static By nameCell(String name) {
        return By.xpath("//td[starts-with(@id,'course-name-') and normalize-space(.)=" + xpathLiteral(name) + "]");
    }

    private static By rowButton(long id, String label) {
        return By.xpath("//tr[@id='course-row-" + id + "']//button[normalize-space()='" + label + "']");
    }
}
