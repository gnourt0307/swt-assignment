package com.swellstore.commission.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium WebDriver end-to-end tests — Swell Store Commission Calculator.
 *
 * <p>Tests are derived directly from the SWT301 Group 3 Excel template:
 * <ul>
 *   <li>Sheet 04 — Test Cases TC-001 to TC-022 (all 22 cases)</li>
 * </ul>
 *
 * <p><strong>Prerequisites:</strong>
 * <ol>
 *   <li>Run {@code mvn jetty:run} in the {@code swell-commission} directory.</li>
 *   <li>Ensure {@code chromedriver} is on your PATH.</li>
 * </ol>
 *
 * <p><strong>Coverage:</strong>
 * <ul>
 *   <li>TC-001 to TC-009: Decision table rules R1–R9</li>
 *   <li>TC-010: Boundary value VB1</li>
 *   <li>TC-011 to TC-018: Invalid-input validation IP1–IP7</li>
 * </ul>
 */
@DisplayName("CommissionCalculator — Selenium UI Tests (TC-001 to TC-018)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommissionSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL  = "http://localhost:8085";
    private static final String INDEX_URL = BASE_URL + "/index.jsp";

    // ------------------------------------------------------------------ //
    //  Setup / Teardown
    // ------------------------------------------------------------------ //

    @BeforeAll
    static void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
        // Uncomment to run headless (no browser window):
        // options.addArguments("--headless", "--disable-gpu");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void pauseForDemonstration() throws InterruptedException {
        Thread.sleep(3000);
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void openForm() {
        driver.get(INDEX_URL);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /**
     * Fill all four fields and submit the form.
     * Pass {@code null} for any dropdown to leave it unselected (default).
     * Pass {@code null} for price to leave the price field untouched (empty).
     */

    private static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void fillAndSubmit(String salaryType, String customerType,
                               String itemType, String price) {
        if (salaryType != null) {
            new Select(driver.findElement(By.id("salaryType")))
                    .selectByValue(salaryType);
        }
        sleep(1000);

        if (customerType != null) {
            new Select(driver.findElement(By.id("customerType")))
                    .selectByValue(customerType);
        }
        sleep(1000);

        if (itemType != null) {
            new Select(driver.findElement(By.id("itemType")))
                    .selectByValue(itemType);
        }
        sleep(1000);

        if (price != null) {
            WebElement priceInput = driver.findElement(By.id("itemPrice"));
            priceInput.clear();
            priceInput.sendKeys(price);
        }
        sleep(1000);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    /**
     * Returns the commission dollar string from result.jsp (e.g. "$25.00").
     * Waits until the result page title appears before reading.
     */
    private String getCommissionText() {
        wait.until(ExpectedConditions.titleContains("Commission Result"));
        WebElement commissionDiv = driver.findElement(By.cssSelector(".commission"));
        String full = commissionDiv.getText(); // "Calculated Commission: $25.00"
        return full.substring(full.lastIndexOf("$")).trim();
    }

    /**
     * Returns all error <li> texts from the errors block on index.jsp.
     * Waits until at least one error element is present.
     */
    private List<String> getErrorMessages() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".errors")));
        return driver.findElements(By.cssSelector(".errors li"))
                     .stream()
                     .map(WebElement::getText)
                     .toList();
    }

    // ================================================================== //
    //  TC-001 to TC-009 — DECISION TABLE RULES (R1–R9)
    // ================================================================== //

    /**
     * TC-001 | Test Design 1.0 | Tags: VP1 VP3 VP5 VP8 VP9 | Type: Normal
     * Rule R1: standard item → $0.00 regardless of other inputs.
     */
    @Test
    @Order(1)
    @DisplayName("TC-001 | R1: salaried + regular + standard + $20 → $0.00")
    void tc001_r1_standard_salaried_regular_zeroCommission() {
        fillAndSubmit("salaried", "regular", "standard", "20");
        assertEquals("$0.00", getCommissionText());
    }

    /**
     * TC-002 | Test Design 2.0 | Tags: VP1 VP3 VP6 VP8 VP9 | Type: Normal
     * Rule R2: regular customer + non-standard item → $0.00.
     */
    @Test
    @Order(2)
    @DisplayName("TC-002 | R2: salaried + regular + bonus + $20 → $0.00")
    void tc002_r2_regularCustomer_bonus_zeroCommission() {
        fillAndSubmit("salaried", "regular", "bonus", "20");
        assertEquals("$0.00", getCommissionText());
    }

    // ================================================================== //
    //  TC-003 to TC-009 — DECISION TABLE RULES R3–R9 (continued)
    // ================================================================== //

    /**
     * TC-003 | Test Design 3.0 | Tags: VP1 VP4 VP6 VP8 VP9 | Type: Normal
     * Rule R3: salaried + non-regular + bonus + $1,000 (≤ $1,000) → 5% = $50.00.
     */
    @Test
    @Order(3)
    @DisplayName("TC-003 | R3: salaried + non-regular + bonus + $1,000 → $50.00 (5%)")
    void tc003_r3_salaried_nonRegular_bonus_300_returns15() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1000");
        assertEquals("$50.00", getCommissionText());
    }

    /**
     * TC-004 | Test Design 4.0 | Tags: VP1 VP4 VP6 VP8 VP10 | Type: Normal
     * Rule R4: salaried + non-regular + bonus + $1,001 (> $1,000) → $25.00 flat.
     */
    @Test
    @Order(4)
    @DisplayName("TC-004 | R4: salaried + non-regular + bonus + $1,001 → $25.00 flat")
    void tc004_r4_salaried_nonRegular_bonus_8000_returnsFlat25() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1001");
        assertEquals("$25.00", getCommissionText());
    }

    /**
     * TC-005 | Test Design 5.0 | Tags: VP2 VP4 VP6 VP8 VP9 | Type: Normal
     * Rule R5: non-salaried + non-regular + bonus + $1,000 (≤ $1,000) → 10% = $100.00.
     */
    @Test
    @Order(5)
    @DisplayName("TC-005 | R5: non-salaried + non-regular + bonus + $1,000 → $100.00 (10%)")
    void tc005_r5_nonSalaried_nonRegular_bonus_500_returns50() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "1000");
        assertEquals("$100.00", getCommissionText());
    }

    /**
     * TC-006 | Test Design 6.0 | Tags: VP2 VP4 VP6 VP9 VP10 | Type: Normal
     * Rule R6: non-salaried + non-regular + bonus + $1,001 (> $1,000) → $75.00 flat.
     */
    @Test
    @Order(6)
    @DisplayName("TC-006 | R6: non-salaried + non-regular + bonus + $1,001 → $75.00 flat")
    void tc006_r6_nonSalaried_nonRegular_bonus_8000_returnsFlat75() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "1001");
        assertEquals("$75.00", getCommissionText());
    }

    /**
     * TC-007 | Test Design 7.0 | Tags: VP2 VP4 VP7 VP9 VP11 | Type: Normal
     * Rule R7: non-salaried + non-regular + other + $10,000 (≤ $10,000) → 10% = $1,000.00.
     */
    @Test
    @Order(7)
    @DisplayName("TC-007 | R7: non-salaried + non-regular + other + $10,000 → $1000.00 (10%)")
    void tc007_r7_nonSalaried_nonRegular_other_5000_returns500() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10000");
        assertEquals("$1000.00", getCommissionText());
    }

    /**
     * TC-008 | Test Design 8.0 | Tags: VP2 VP4 VP7 VP10 VP12 | Type: Normal
     * Rule R8: non-salaried + non-regular + other + $10,001 (> $10,000) → 5% = $500,05.
     */
    @Test
    @Order(8)
    @DisplayName("TC-008 | R8: non-salaried + non-regular + other + $10,001 → $500.05 (5%)")
    void tc008_r8_nonSalaried_nonRegular_other_15000_returns750() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10001");
        assertEquals("$500.05", getCommissionText());
    }

    /**
     * TC-009 | Test Design 9.0 | Tags: VP1 VP4 VP7 VP9 | Type: Normal
     * Rule R9: salaried + non-regular + other → $0.00 (policy silent).
     */
    @Test
    @Order(9)
    @DisplayName("TC-009 | R9: salaried + non-regular + other + $8,000 → $0.00")
    void tc009_r9_salaried_nonRegular_other_zeroCommission() {
        fillAndSubmit("salaried", "non-regular", "other", "8000");
        assertEquals("$0.00", getCommissionText());
    }

    // ================================================================== //
    //  TC-010 — BOUNDARY VALUE TESTS (VB1)
    // ================================================================== //

    /**
     * TC-010 | Test Design 10.0 | Tags: VP1 VP4 VP6 VB1 | Type: Boundary
     * VB1: minimum valid price $1 → R3 → 5% × $1 = $0.05.
     */
    @Test
    @Order(10)
    @DisplayName("TC-010 | VB1: salaried + non-regular + bonus + $1 (minimum valid) → $0.05 (5%)")
    void tc010_vb1_minimumPrice_returns0_05() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1");
        assertEquals("$0.05", getCommissionText());
    }

    // ================================================================== //
    //  TC-011 to TC-018 — VALIDATION / INVALID-INPUT TESTS (IP1–IP7)
    // ================================================================== //

    /**
     * TC-011 | Test Design 11.0 | Tags: IP4 | Type: Abnormal
     * IP4: item price left empty → servlet returns error "Item price is required."
     */
    @Test
    @Order(11)
    @DisplayName("TC-011 | IP4: item price empty → error 'Item price is required.'")
    void tc011_ip4_emptyPrice_showsRequiredError() {
        fillAndSubmit("salaried", "non-regular", "bonus", "");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.contains("Item price is required")),
            "Expected 'Item price is required.' in errors but got: " + errors
        );
    }

    /**
     * TC-012 | Test Design 12.0 | Tags: IP5 | Type: Abnormal
     * IP5: item price is non-numeric ("abc") → error "Item price must be a valid number."
     */
    @Test
    @Order(12)
    @DisplayName("TC-012 | IP5: item price 'abc' → error 'Item price must be a valid number.'")
    void tc012_ip5_nonNumericPrice_showsValidNumberError() {
        fillAndSubmit("salaried", "non-regular", "bonus", "abc");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.contains("valid number")),
            "Expected 'valid number' error but got: " + errors
        );
    }

    /**
     * TC-013 | Test Design 13.0 | Tags: IP6 IB1 | Type: Abnormal
     * IP6/IB1: item price = 0 → error "Item price must be greater than zero."
     */
    @Test
    @Order(13)
    @DisplayName("TC-013 | IP6/IB1: item price = 0 → error 'Item price must be greater than zero.'")
    void tc013_ip6_ib1_zeroPriceShowsError() {
        fillAndSubmit("salaried", "non-regular", "bonus", "0");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.contains("greater than zero")),
            "Expected 'greater than zero' error but got: " + errors
        );
    }

    /**
     * TC-014 | Test Design 14.0 | Tags: IP7 IB2 | Type: Abnormal
     * IP7/IB2: item price is negative (-1) → error "Item price must be greater than zero."
     */
    @Test
    @Order(14)
    @DisplayName("TC-014 | IP7/IB2: item price = -1 → error 'Item price must be greater than zero.'")
    void tc014_ip7_ib2_negativePriceShowsError() {
        fillAndSubmit("salaried", "non-regular", "bonus", "-1");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.contains("greater than zero")),
            "Expected 'greater than zero' error but got: " + errors
        );
    }

    /**
     * TC-015 | Test Design 15.0 | Tags: IP1 | Type: Abnormal
     * IP1: salary type not selected → error "Salary type is required..."
     */
    @Test
    @Order(15)
    @DisplayName("TC-015 | IP1: salary type not selected → error 'Salary type is required.'")
    void tc015_ip1_salaryTypeNotSelected_showsError() {
        // null = leave salary type at default (unselected)
        fillAndSubmit(null, "non-regular", "bonus", "10");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.toLowerCase().contains("salary type")),
            "Expected 'Salary type' error but got: " + errors
        );
    }

    /**
     * TC-016 | Test Design 16.0 | Tags: IP2 | Type: Abnormal
     * IP2: customer type not selected → error "Customer type is required."
     */
    @Test
    @Order(16)
    @DisplayName("TC-016 | IP2: customer type not selected → error 'Customer type is required.'")
    void tc016_ip2_customerTypeNotSelected_showsError() {
        fillAndSubmit("salaried", null, "bonus", "10");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.toLowerCase().contains("customer type")),
            "Expected 'Customer type' error but got: " + errors
        );
    }

    /**
     * TC-017 | Test Design 17.0 | Tags: IP3 | Type: Abnormal
     * IP3: item type not selected → error "Item type is required."
     */
    @Test
    @Order(17)
    @DisplayName("TC-017 | IP3: item type not selected → error 'Item type is required.'")
    void tc017_ip3_itemTypeNotSelected_showsError() {
        fillAndSubmit("salaried", "non-regular", null, "10");
        List<String> errors = getErrorMessages();
        assertTrue(
            errors.stream().anyMatch(e -> e.toLowerCase().contains("item type")),
            "Expected 'Item type' error but got: " + errors
        );
    }

    /**
     * TC-018 | Test Design 18.0 | Tags: IP1 IP2 IP3 IP4 | Type: Abnormal
     * Combined: all four fields missing → all 4 error messages displayed simultaneously.
     * Form should be re-shown with all fields empty (no sticky-values fill-in).
     */
    @Test
    @Order(18)
    @DisplayName("TC-018 | IP1+IP2+IP3+IP4: all fields missing → exactly 4 errors displayed")
    void tc018_allFieldsMissing_showsFourErrors() {
        // Submit without touching any field
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        List<String> errors = getErrorMessages();
        assertEquals(
            4, errors.size(),
            "Expected exactly 4 validation errors but got " + errors.size() + ": " + errors
        );
    }
}
