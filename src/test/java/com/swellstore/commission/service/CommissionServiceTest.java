package com.swellstore.commission.service;

import com.swellstore.commission.model.CommissionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 unit tests for {@link CommissionService}.
 *
 * <p>Tests are derived directly from the SWT301 Group 3 Excel template:
 * <ul>
 *   <li>Sheet 01 — Decision Table (UTCID01–UTCID09)</li>
 *   <li>Sheet 02 — Condition Analysis (VP1–VP12, VB1–VB5)</li>
 *   <li>Sheet 03 — Test Design (cases 1.0–14.0)</li>
 *   <li>Sheet 04 — Test Cases (TC-001 to TC-014)</li>
 * </ul>
 *
 * <p>TC-015 to TC-022 (validation / invalid-input scenarios) require the
 * HTTP servlet layer and are covered by {@code CommissionSeleniumTest}.
 *
 * <p>Partition coverage:
 * <pre>
 *   Valid  : VP1 VP2 VP3 VP4 VP5 VP6 VP7 VP8 VP9 VP10 VP11 VP12
 *   Bounds : VB1 VB2 VB3 VB4 VB5
 * </pre>
 */
@DisplayName("CommissionService — Decision Table & Boundary Tests")
class CommissionServiceTest {

    private static final double DELTA = 0.001;

    /** Convenience factory. */
    private double calc(String salary, String customer, String item, double price) {
        return CommissionService.calculate(
                new CommissionRequest(salary, customer, item, price));
    }

    // ====================================================================
    // DECISION TABLE RULES — TC-001 to TC-009
    // Decision Table sheet: UTCID01–UTCID09
    // ====================================================================

    /**
     * TC-001 | Test Design 1.0 | Tags: VP1 VP3 VP5 VP8 VP9
     * Rule R1: standard item always yields $0.00 regardless of other inputs.
     * Inputs: salaried, regular, standard, $20
     */
    @Test
    @DisplayName("TC-001 | R1: salaried + regular + standard + $20 → $0.00")
    void tc001_r1_standard_salaried_regular_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "standard", 20.0), DELTA);
    }

    /**
     * TC-002 | Test Design 2.0 | Tags: VP1 VP3 VP6 VP8 VP9
     * Rule R2: regular customer always yields $0.00 (non-standard item).
     * Inputs: salaried, regular, bonus, $20
     */
    @Test
    @DisplayName("TC-002 | R2: salaried + regular + bonus + $20 → $0.00")
    void tc002_r2_regularCustomer_bonus_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "bonus", 20.0), DELTA);
    }

    /**
     * TC-003 | Test Design 3.0 | Tags: VP1 VP4 VP6 VP8 VP9
     * Rule R3: salaried + non-regular + bonus + price ≤ $1,000 → 5% of price.
     * Inputs: salaried, non-regular, bonus, $300 → 5% × 300 = $15.00
     */
    @Test
    @DisplayName("TC-003 | R3: salaried + non-regular + bonus + $300 → $15.00 (5%)")
    void tc003_r3_salaried_nonRegular_bonus_price300_returns15() {
        assertEquals(15.0, calc("salaried", "non-regular", "bonus", 300.0), DELTA);
    }

    /**
     * TC-004 | Test Design 4.0 | Tags: VP1 VP4 VP6 VP8 VP10
     * Rule R4: salaried + non-regular + bonus + price > $1,000 → $25.00 flat.
     * Inputs: salaried, non-regular, bonus, $8,000
     */
    @Test
    @DisplayName("TC-004 | R4: salaried + non-regular + bonus + $8,000 → $25.00 flat")
    void tc004_r4_salaried_nonRegular_bonus_price8000_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 8000.0), DELTA);
    }

    /**
     * TC-005 | Test Design 5.0 | Tags: VP2 VP4 VP6 VP8 VP9
     * Rule R5: non-salaried + non-regular + bonus + price ≤ $1,000 → 10% of price.
     * Inputs: non-salaried, non-regular, bonus, $500 → 10% × 500 = $50.00
     */
    @Test
    @DisplayName("TC-005 | R5: non-salaried + non-regular + bonus + $500 → $50.00 (10%)")
    void tc005_r5_nonSalaried_nonRegular_bonus_price500_returns50() {
        assertEquals(50.0, calc("non-salaried", "non-regular", "bonus", 500.0), DELTA);
    }

    /**
     * TC-006 | Test Design 6.0 | Tags: VP2 VP4 VP6 VP9 VP10
     * Rule R6: non-salaried + non-regular + bonus + price > $1,000 → $75.00 flat.
     * Inputs: non-salaried, non-regular, bonus, $8,000
     */
    @Test
    @DisplayName("TC-006 | R6: non-salaried + non-regular + bonus + $8,000 → $75.00 flat")
    void tc006_r6_nonSalaried_nonRegular_bonus_price8000_returnsFlat75() {
        assertEquals(75.0, calc("non-salaried", "non-regular", "bonus", 8000.0), DELTA);
    }

    /**
     * TC-007 | Test Design 7.0 | Tags: VP2 VP4 VP7 VP9 VP11
     * Rule R7: non-salaried + non-regular + other + price ≤ $10,000 → 10% of price.
     * Inputs: non-salaried, non-regular, other, $5,000 → 10% × 5000 = $500.00
     */
    @Test
    @DisplayName("TC-007 | R7: non-salaried + non-regular + other + $5,000 → $500.00 (10%)")
    void tc007_r7_nonSalaried_nonRegular_other_price5000_returns500() {
        assertEquals(500.0, calc("non-salaried", "non-regular", "other", 5000.0), DELTA);
    }

    /**
     * TC-008 | Test Design 8.0 | Tags: VP2 VP4 VP7 VP10 VP12
     * Rule R8: non-salaried + non-regular + other + price > $10,000 → 5% of price.
     * Inputs: non-salaried, non-regular, other, $15,000 → 5% × 15000 = $750.00
     */
    @Test
    @DisplayName("TC-008 | R8: non-salaried + non-regular + other + $15,000 → $750.00 (5%)")
    void tc008_r8_nonSalaried_nonRegular_other_price15000_returns750() {
        assertEquals(750.0, calc("non-salaried", "non-regular", "other", 15000.0), DELTA);
    }

    /**
     * TC-009 | Test Design 9.0 | Tags: VP1 VP4 VP7 VP9
     * Rule R9: salaried + non-regular + other → $0.00 (policy silent).
     * Inputs: salaried, non-regular, other, $8,000
     */
    @Test
    @DisplayName("TC-009 | R9: salaried + non-regular + other + $8,000 → $0.00")
    void tc009_r9_salaried_nonRegular_other_returnsZero() {
        assertEquals(0.0, calc("salaried", "non-regular", "other", 8000.0), DELTA);
    }

    // ====================================================================
    // BOUNDARY VALUE TESTS — TC-010 to TC-014
    // Condition sheet: VB1–VB5
    // ====================================================================

    /**
     * TC-010 | Test Design 10.0 | Tags: VP1 VP4 VP6 VB2
     * VB2: bonus price = $1,000 exactly → rule R3 applies (≤ threshold) → 5%.
     * 5% × $1,000 = $50.00  (must NOT fall through to R4 flat $25.00)
     */
    @Test
    @DisplayName("TC-010 | VB2: salaried + non-regular + bonus + $1,000 exactly → $50.00 (5%, not flat)")
    void tc010_vb2_salaried_nonRegular_bonus_atBonusThreshold_returns50() {
        assertEquals(50.0, calc("salaried", "non-regular", "bonus", 1000.0), DELTA);
    }

    /**
     * TC-011 | Test Design 11.0 | Tags: VP1 VP4 VP6 VB3
     * VB3: bonus price = $1,001 (just above threshold) → rule R4 applies → $25.00 flat.
     */
    @Test
    @DisplayName("TC-011 | VB3: salaried + non-regular + bonus + $1,001 → $25.00 flat")
    void tc011_vb3_salaried_nonRegular_bonus_justAboveBonusThreshold_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 1001.0), DELTA);
    }

    /**
     * TC-012 | Test Design 12.0 | Tags: VP2 VP4 VP7 VB4
     * VB4: other price = $10,000 exactly → rule R7 applies (≤ threshold) → 10%.
     * 10% × $10,000 = $1,000.00  (must NOT fall through to R8 at 5%)
     */
    @Test
    @DisplayName("TC-012 | VB4: non-salaried + non-regular + other + $10,000 exactly → $1,000.00 (10%, not 5%)")
    void tc012_vb4_nonSalaried_nonRegular_other_atOtherThreshold_returns1000() {
        assertEquals(1000.0, calc("non-salaried", "non-regular", "other", 10000.0), DELTA);
    }

    /**
     * TC-013 | Test Design 13.0 | Tags: VP2 VP4 VP7 VB5
     * VB5: other price = $10,001 (just above threshold) → rule R8 applies → 5%.
     * 5% × $10,001 = $500.05
     */
    @Test
    @DisplayName("TC-013 | VB5: non-salaried + non-regular + other + $10,001 → $500.05 (5%)")
    void tc013_vb5_nonSalaried_nonRegular_other_justAboveOtherThreshold_returns500_05() {
        assertEquals(500.05, calc("non-salaried", "non-regular", "other", 10001.0), DELTA);
    }

    /**
     * TC-014 | Test Design 14.0 | Tags: VP1 VP4 VP6 VB1
     * VB1: minimum valid price = $1 → rule R3 → 5% × $1 = $0.05.
     */
    @Test
    @DisplayName("TC-014 | VB1: salaried + non-regular + bonus + $1 (minimum) → $0.05 (5%)")
    void tc014_vb1_salaried_nonRegular_bonus_minPrice_returns0_05() {
        assertEquals(0.05, calc("salaried", "non-regular", "bonus", 1.0), DELTA);
    }

    // ====================================================================
    // COVERAGE COMPLETION — defensive guard branch
    // ====================================================================

    /**
     * Coverage test for the {@code throw new IllegalArgumentException} guard in
     * {@link CommissionService#calculate}.
     *
     * <p>This branch is unreachable via the web form (the {@code <select>} dropdown
     * only allows "standard", "bonus", or "other"), so it has no corresponding Excel
     * test case.  It is tested here solely to achieve 100% branch coverage in JaCoCo.
     *
     * <p>If this branch were ever reached in production it would indicate a programming
     * error (invalid data bypassing servlet validation), hence the guard is correct
     * and should stay in the code.
     */
    @Test
    @DisplayName("Coverage | unknown item type → IllegalArgumentException (defensive guard)")
    void coverage_unknownItemType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                CommissionService.calculate(
                        new CommissionRequest("salaried", "non-regular", "UNKNOWN", 100.0)));
    }
}
