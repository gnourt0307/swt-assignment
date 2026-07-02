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

@DisplayName("CommissionCalculator — Selenium UI Tests (TC-001 to TC-018)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommissionSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL  = "http://localhost:8080";
    private static final String INDEX_URL = BASE_URL + "/index.jsp";

    @BeforeAll
    static void setUpDriver() {
        ChromeOptions options = new ChromeOptions();
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

    //helpers

    private static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void fillAndSubmit(String salaryType, String customerType,
                               String itemType, String price) {
        if (salaryType != null) {
            new Select(driver.findElement(By.id("salaryType")))
                    .selectByValue(salaryType);
        }
//        sleep(1000);

        if (customerType != null) {
            new Select(driver.findElement(By.id("customerType")))
                    .selectByValue(customerType);
        }
//        sleep(1000);

        if (itemType != null) {
            new Select(driver.findElement(By.id("itemType")))
                    .selectByValue(itemType);
        }
//        sleep(1000);

        if (price != null) {
            WebElement priceInput = driver.findElement(By.id("itemPrice"));
            priceInput.clear();
            priceInput.sendKeys(price);
        }
//        sleep(1000);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    private String getCommissionText() {
        wait.until(ExpectedConditions.titleContains("Commission Result"));
        WebElement commissionDiv = driver.findElement(By.cssSelector(".commission"));
        String full = commissionDiv.getText();
        return full.substring(full.lastIndexOf("$")).trim();
    }

    private List<String> getErrorMessages() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".errors")));
        return driver.findElements(By.cssSelector(".errors li"))
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    @Test
    @Order(1)
    @DisplayName("TC-01: salaried + regular + standard + $20 => $0.00")
    void tc01() {
        fillAndSubmit("salaried", "regular", "standard", "20");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(2)
    @DisplayName("TC-02: salaried + regular + bonus + $20 => $0.00")
    void tc02() {
        fillAndSubmit("salaried", "regular", "bonus", "20");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(3)
    @DisplayName("TC-003: salaried + non-regular + bonus + $1,000 => $50.00 (5%)")
    void tc03() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1000");
        assertEquals("$50.00", getCommissionText());
    }

    @Test
    @Order(4)
    @DisplayName("TC-004: salaried + non-regular + bonus + $1,001 => $25.00 flat")
    void tc04() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1001");
        assertEquals("$25.00", getCommissionText());
    }

    @Test
    @Order(5)
    @DisplayName("TC-005: non-salaried + non-regular + bonus + $1,000 => $100.00 (10%)")
    void tc05() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "1000");
        assertEquals("$100.00", getCommissionText());
    }

    @Test
    @Order(6)
    @DisplayName("TC-06: non-salaried + non-regular + bonus + $1,001 => $75.00 flat")
    void tc06() {
        fillAndSubmit("non-salaried", "non-regular", "bonus", "1001");
        assertEquals("$75.00", getCommissionText());
    }

    @Test
    @Order(7)
    @DisplayName("TC-07: non-salaried + non-regular + other + $10,000 => $1000.00 (10%)")
    void tc07() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10000");
        assertEquals("$1000.00", getCommissionText());
    }

    @Test
    @Order(8)
    @DisplayName("TC-08: non-salaried + non-regular + other + $10,001 => $500.05 (5%)")
    void tc08() {
        fillAndSubmit("non-salaried", "non-regular", "other", "10001");
        assertEquals("$500.05", getCommissionText());
    }

    @Test
    @Order(9)
    @DisplayName("TC-09: salaried + non-regular + other + $8,000 => $0.00")
    void tc09() {
        fillAndSubmit("salaried", "non-regular", "other", "8000");
        assertEquals("$0.00", getCommissionText());
    }

    @Test
    @Order(10)
    @DisplayName("TC-10: salaried + non-regular + bonus + $1 => $0.05 (5%)")
    void tc10() {
        fillAndSubmit("salaried", "non-regular", "bonus", "1");
        assertEquals("$0.05", getCommissionText());
    }

    @Test
    @Order(11)
    @DisplayName("TC-11: item price empty => error 'Item price is required.'")
    void tc11() {
        fillAndSubmit("salaried", "non-regular", "bonus", "");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.contains("Item price is required")),
                "Expected 'Item price is required.' in errors but got: " + errors
        );
    }

    @Test
    @Order(12)
    @DisplayName("TC-12: item price 'abc' => error 'Item price must be a valid number.'")
    void tc12() {
        fillAndSubmit("salaried", "non-regular", "bonus", "abc");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.contains("valid number")),
                "Expected 'valid number' error but got: " + errors
        );
    }

    @Test
    @Order(13)
    @DisplayName("TC-13: item price = 0 => error 'Item price must be greater than zero.'")
    void tc13() {
        fillAndSubmit("salaried", "non-regular", "bonus", "0");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.contains("greater than zero")),
                "Expected 'greater than zero' error but got: " + errors
        );
    }

    @Test
    @Order(14)
    @DisplayName("TC-14: item price = -1 => error 'Item price must be greater than zero.'")
    void tc14() {
        fillAndSubmit("salaried", "non-regular", "bonus", "-1");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.contains("greater than zero")),
                "Expected 'greater than zero' error but got: " + errors
        );
    }

    @Test
    @Order(15)
    @DisplayName("TC-15: salary type not selected => error 'Salary type is required.'")
    void tc15() {
        // null = leave salary type at default (unselected)
        fillAndSubmit(null, "non-regular", "bonus", "10");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.toLowerCase().contains("salary type")),
                "Expected 'Salary type' error but got: " + errors
        );
    }

    @Test
    @Order(16)
    @DisplayName("TC-16: customer type not selected => error 'Customer type is required.'")
    void tc16() {
        fillAndSubmit("salaried", null, "bonus", "10");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.toLowerCase().contains("customer type")),
                "Expected 'Customer type' error but got: " + errors
        );
    }

    @Test
    @Order(17)
    @DisplayName("TC-17: item type not selected => error 'Item type is required.'")
    void tc17() {
        fillAndSubmit("salaried", "non-regular", null, "10");
        List<String> errors = getErrorMessages();
        assertTrue(
                errors.stream().anyMatch(e -> e.toLowerCase().contains("item type")),
                "Expected 'Item type' error but got: " + errors
        );
    }

    @Test
    @Order(18)
    @DisplayName("TC-18: all fields missing => 4 errors displayed")
    void tc18() {
        // Submit without touching any field
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        List<String> errors = getErrorMessages();
        assertEquals(
                4, errors.size(),
                "Expected exactly 4 validation errors but got " + errors.size() + ": " + errors
        );
    }
}
