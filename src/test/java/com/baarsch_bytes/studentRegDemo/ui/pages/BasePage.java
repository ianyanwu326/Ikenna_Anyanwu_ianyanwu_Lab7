package com.baarsch_bytes.studentRegDemo.ui.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Behaviour shared by both page objects: typing, reading fields, alerts and error messages. */
abstract class BasePage {

    /** Anything a developer could reasonably use to show an error to the user. */
    private static final By ERROR_MESSAGE = By.cssSelector(
            "[role='alert'], [aria-live], .error, .error-message, [id*='error' i], [data-testid*='error' i]");

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final String baseUrl;

    BasePage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    protected void type(By locator, String text) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        input.clear();
        input.sendKeys(text);
    }

    /** What is actually in an input (the DOM property, not the HTML attribute). */
    protected String valueOf(By locator) {
        return driver.findElement(locator).getDomProperty("value");
    }

    protected static String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }

    // ---- navigation bar (same on every page) ------------------------------------------

    public void goToStudentsViaNav() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-student-list-link"))).click();
    }

    public void goToCoursesViaNav() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-course-list-link"))).click();
    }

    public String headingText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))).getText();
    }

    /** True if an error element is visible right now (does not wait). */
    protected boolean errorVisibleNow() {
        return driver.findElements(ERROR_MESSAGE).stream()
                .anyMatch(e -> e.isDisplayed() && !e.getText().isBlank());
    }

    /** Waits (up to the default timeout) for a condition; returns false instead of throwing on timeout. */
    protected boolean becomesTrue(BooleanSupplier condition) {
        try {
            wait.until(d -> condition.getAsBoolean());
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    // ---- knowing when the server has handled a request ----------------------------------
    // A rejected action changes nothing on screen, so a test can't wait for "the new row".
    // Instead it counts the fetch() calls the page has finished and waits for the one it
    // triggered. Call trackRequests() after the page has loaded, before the action.

    public void trackRequests() {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "if (!window.__requestsDone) { window.__requestsDone = 0; const original = window.fetch;"
                        + " window.fetch = function () { return original.apply(this, arguments)"
                        + ".finally(() => { window.__requestsDone++; }); }; }");
    }

    public void waitForRequestsDone(int count) {
        wait.until(d -> {
            Object done = ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return window.__requestsDone || 0;");
            return ((Number) done).intValue() >= count;
        });
    }

    // ---- JavaScript alert() handling -------------------------------------------------

    /** Waits for a browser alert, accepts it, and returns its text. */
    public String acceptAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String text = alert.getText();
        alert.accept();
        return text;
    }

    // ---- error messages ---------------------------------------------------------------

    /**
     * True if, within a few seconds, the page shows the user an error: either a visible
     * error element, or a browser alert (which is accepted). Used by the defect tests.
     */
    public boolean userIsToldOfFailure() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
        try {
            return shortWait.until(d -> {
                try {
                    Alert alert = d.switchTo().alert();
                    alert.accept();
                    return true;
                } catch (NoAlertPresentException ignored) {
                    // fall through to look for an element
                }
                List<WebElement> found = d.findElements(ERROR_MESSAGE);
                return found.stream().anyMatch(e -> e.isDisplayed() && !e.getText().isBlank());
            });
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}
