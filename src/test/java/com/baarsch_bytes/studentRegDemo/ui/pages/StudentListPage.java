package com.baarsch_bytes.studentRegDemo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * A Page Object for the Students page. Tests call methods named after what a user does,
 * and only this class knows how to find elements. When the page changes, you fix one class
 * instead of every test.
 *
 * Extended for Lab 7: row-scoped Edit/Delete (their ids repeat on every row, so they are
 * found through the row), edit form actions, settle-waits that need no fixed sleeps, and
 * read-back helpers that use getDomProperty("value").
 */
public class StudentListPage extends BasePage {

    private static final By TABLE = By.id("student-list-table");
    private static final By NAME_INPUT = By.id("new-student-name");
    private static final By MAJOR_INPUT = By.id("new-student-major");
    private static final By GPA_INPUT = By.id("new-student-gpa");
    private static final By ADD_BUTTON = By.cssSelector("#add-student-fields button");

    private static final By EDIT_NAME = By.id("edit-student-name");
    private static final By EDIT_MAJOR = By.id("edit-student-major");
    private static final By EDIT_GPA = By.id("edit-student-gpa");
    private static final By EDIT_SAVE = By.id("edit-student-save-button");
    private static final By EDIT_CANCEL = By.id("edit-student-cancel-button");

    public StudentListPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        super(driver, wait, baseUrl);
    }

    /** Opens the page and waits until the student table has loaded. */
    public StudentListPage open() {
        driver.get(baseUrl + "/students");
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        return this;
    }

    /** Waits for the table; for use after navigating with the nav bar. */
    public StudentListPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE));
        return this;
    }

    // ---- adding ------------------------------------------------------------------------

    /** Fills in the new-student form and submits it. Doesn't wait for the result. */
    public void submitNewStudent(String name, String major, String gpa) {
        fillNewStudentForm(name, major, gpa);
        wait.until(ExpectedConditions.elementToBeClickable(ADD_BUTTON)).click();
    }

    public void fillNewStudentForm(String name, String major, String gpa) {
        type(NAME_INPUT, name);
        type(MAJOR_INPUT, major);
        type(GPA_INPUT, gpa);
    }

    /**
     * Waits until the add action has finished, whichever way it ended: the form has been
     * emptied (success) or an error is showing (a corrected UI that keeps the input).
     */
    public void waitForAddToSettle() {
        wait.until(d -> addFormIsEmpty() || errorVisibleNow());
    }

    public boolean addFormBecomesEmpty() { return becomesTrue(this::addFormIsEmpty); }

    public boolean addFormIsEmpty() {
        return valueOf(NAME_INPUT).isEmpty() && valueOf(MAJOR_INPUT).isEmpty() && valueOf(GPA_INPUT).isEmpty();
    }

    public String newNameValue() { return valueOf(NAME_INPUT); }
    public String newMajorValue() { return valueOf(MAJOR_INPUT); }
    public String newGpaValue() { return valueOf(GPA_INPUT); }

    // ---- reading the table -------------------------------------------------------------

    /**
     * Waits until a row for the student with this exact name appears, and returns its id.
     * Rows are identified by database id, which a test can't know in advance, so we find
     * the row through its name cell.
     */
    public long waitForStudentNamed(String name) {
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(nameCell(name)));
        return Long.parseLong(cell.getDomAttribute("id").substring("student-name-".length()));
    }

    /** How many rows have exactly this name right now (no waiting). */
    public int countRowsNamed(String name) {
        return driver.findElements(nameCell(name)).size();
    }

    public boolean hasRow(long id) {
        return !driver.findElements(By.id("student-row-" + id)).isEmpty();
    }

    public void waitUntilRowIsGone(long id) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("student-row-" + id)));
    }

    public void waitForRow(long id) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-row-" + id)));
    }

    public String nameOf(long id) { return driver.findElement(By.id("student-name-" + id)).getText(); }
    public String majorOf(long id) { return driver.findElement(By.id("student-major-" + id)).getText(); }
    public String gpaOf(long id) { return driver.findElement(By.id("student-gpa-" + id)).getText(); }

    public void waitForNameOf(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("student-name-" + id), expected));
    }
    public void waitForMajorOf(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("student-major-" + id), expected));
    }
    public void waitForGpaOf(long id, String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("student-gpa-" + id), expected));
    }

    public List<WebElement> rows() {
        return driver.findElements(By.cssSelector("#student-list-table tbody tr[id^='student-row-']"));
    }

    /** The column headings, in order. */
    public List<String> columnHeadings() {
        return driver.findElements(By.cssSelector("#student-list-table thead th")).stream()
                .map(WebElement::getText).toList();
    }

    // ---- editing -----------------------------------------------------------------------

    /** Clicks Edit in this student's row (the Edit buttons all share one id, so scope to the row). */
    public void clickEdit(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(rowButton(id, "Edit"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EDIT_NAME));
    }

    public String editNameValue() { return valueOf(EDIT_NAME); }
    public String editMajorValue() { return valueOf(EDIT_MAJOR); }
    public String editGpaValue() { return valueOf(EDIT_GPA); }

    public void setEditName(String v) { type(EDIT_NAME, v); }
    public void setEditMajor(String v) { type(EDIT_MAJOR, v); }
    public void setEditGpa(String v) { type(EDIT_GPA, v); }

    public void saveEdit() { wait.until(ExpectedConditions.elementToBeClickable(EDIT_SAVE)).click(); }
    public void cancelEdit() { wait.until(ExpectedConditions.elementToBeClickable(EDIT_CANCEL)).click(); }

    public boolean editFormIsOpen() { return !driver.findElements(EDIT_NAME).isEmpty(); }

    public void waitForEditFormToClose() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(EDIT_NAME));
    }

    /** Waits until the edit action has finished: form closed (success) or an error is showing. */
    public void waitForEditToSettle() {
        wait.until(d -> !editFormIsOpen() || errorVisibleNow());
    }

    // ---- deleting ----------------------------------------------------------------------

    public void clickDelete(long id) {
        wait.until(ExpectedConditions.elementToBeClickable(rowButton(id, "Delete"))).click();
    }

    // ---- helpers -----------------------------------------------------------------------

    private static By nameCell(String name) {
        return By.xpath("//td[starts-with(@id,'student-name-') and normalize-space(.)=" + xpathLiteral(name) + "]");
    }

    private static By rowButton(long id, String label) {
        return By.xpath("//tr[@id='student-row-" + id + "']//button[normalize-space()='" + label + "']");
    }
}
