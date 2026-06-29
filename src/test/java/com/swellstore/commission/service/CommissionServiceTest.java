package com.swellstore.commission.service;

import com.swellstore.commission.model.CommissionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CommissionService — Decision Table & Boundary Tests")
class CommissionServiceTest {

    private static final double DELTA = 0.001;

    /** Convenience factory. */
    private double calc(String salary, String customer, String item, double price) {
        return CommissionService.calculate(
                new CommissionRequest(salary, customer, item, price));
    }

    @Test
    @DisplayName("TC-001 | R1: salaried + regular + standard + $1000 → $0.00")
    void tc001_r1_standard_salaried_regular_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "standard", 1000.0), DELTA);
    }

    @Test
    @DisplayName("TC-002 | R2: salaried + regular + bonus + $1000 → $0.00")
    void tc002_r2_regularCustomer_bonus_returnsZero() {
        assertEquals(0.0, calc("salaried", "regular", "bonus", 1000.0), DELTA);
    }

    @Test
    @DisplayName("TC-003 | R3: salaried + non-regular + bonus + $1000 → $50.00 (5%)")
    void tc003_r3_salaried_nonRegular_bonus_price1000_returns50() {
        assertEquals(50.0, calc("salaried", "non-regular", "bonus", 1000.0), DELTA);
    }

    @Test
    @DisplayName("TC-004 | R4: salaried + non-regular + bonus + $10,000 → $25.00 flat")
    void tc004_r4_salaried_nonRegular_bonus_price10000_returnsFlat25() {
        assertEquals(25.0, calc("salaried", "non-regular", "bonus", 10000.0), DELTA);
    }

    @Test
    @DisplayName("TC-005 | R5: non-salaried + non-regular + bonus + $1000 → $100.00 (10%)")
    void tc005_r5_nonSalaried_nonRegular_bonus_price1000_returns100() {
        assertEquals(100.0, calc("non-salaried", "non-regular", "bonus", 1000.0), DELTA);
    }

    @Test
    @DisplayName("TC-006 | R6: non-salaried + non-regular + bonus + $10,000 → $75.00 flat")
    void tc006_r6_nonSalaried_nonRegular_bonus_price10000_returnsFlat75() {
        assertEquals(75.0, calc("non-salaried", "non-regular", "bonus", 10000.0), DELTA);
    }

    @Test
    @DisplayName("TC-007 | R7: non-salaried + non-regular + other + $15,000 → $1500.00 (10%)")
    void tc007_r7_nonSalaried_nonRegular_other_price15000_returns1500() {
        assertEquals(1500.0, calc("non-salaried", "non-regular", "other", 15000.0), DELTA);
    }

    @Test
    @DisplayName("TC-008 | R8: non-salaried + non-regular + other + $15,000 → $750.00 (5%)")
    void tc008_r8_nonSalaried_nonRegular_other_price15000_returns750() {
        assertEquals(750.0, calc("non-salaried", "non-regular", "other", 15000.0), DELTA);
    }

    @Test
    @DisplayName("TC-009 | R9: salaried + non-regular + other + $10,000 → $0.00")
    void tc009_r9_salaried_nonRegular_other_returnsZero() {
        assertEquals(0.0, calc("salaried", "non-regular", "other", 10000.0), DELTA);
    }

    @Test
    @DisplayName("Coverage | unknown item type → IllegalArgumentException (defensive guard)")
    void coverage_unknownItemType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                CommissionService.calculate(
                        new CommissionRequest("salaried", "non-regular", "UNKNOWN", 100.0)));
    }
}
