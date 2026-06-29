package com.swellstore.commission.service;

import com.swellstore.commission.model.CommissionRequest;

public class CommissionService {

    // ------------------------------------------------------------------ //
    //  Named constants for thresholds and commission rates (spec §11)
    // ------------------------------------------------------------------ //

    private static final double BONUS_PRICE_THRESHOLD  = 1000.0;
    private static final double OTHER_PRICE_THRESHOLD  = 10000.0;
    private static final double SALARIED_BONUS_PCT     = 0.05;
    private static final double SALARIED_BONUS_FLAT    = 25.0;
    private static final double NONSALARIED_BONUS_PCT  = 0.10;
    private static final double NONSALARIED_BONUS_FLAT = 75.0;
    private static final double NONSALARIED_OTHER_LOW  = 0.10;
    private static final double NONSALARIED_OTHER_HIGH = 0.05;

    // Utility class — no instantiation needed.
    private CommissionService() {}

    public static double calculate(CommissionRequest req) {

        // ---- R1: Standard item → always $0.00 -------------------------
        if ("standard".equals(req.getItemType())) {
            return 0.0;
        }

        // ---- R2: Regular customer → always $0.00 ----------------------
        if ("regular".equals(req.getCustomerType())) {
            return 0.0;
        }

        // From here: non-regular customer + non-standard item

        if ("bonus".equals(req.getItemType())) {

            if ("salaried".equals(req.getSalaryType())) {
                // R3: salaried, bonus, price ≤ 1000 → 5%
                if (req.getItemPrice() <= BONUS_PRICE_THRESHOLD) {
                    return req.getItemPrice() * SALARIED_BONUS_PCT;
                }
                // R4: salaried, bonus, price > 1000 → $25.00 flat
                else {
                    return SALARIED_BONUS_FLAT;
                }

            } else {
                // non-salaried
                // R5: non-salaried, bonus, price ≤ 1000 → 10%
                if (req.getItemPrice() <= BONUS_PRICE_THRESHOLD) {
                    return req.getItemPrice() * NONSALARIED_BONUS_PCT;
                }
                // R6: non-salaried, bonus, price > 1000 → $75.00 flat
                else {
                    return NONSALARIED_BONUS_FLAT;
                }
            }
        }

        if ("other".equals(req.getItemType())) {

            if ("non-salaried".equals(req.getSalaryType())) {
                // R7: non-salaried, other, price ≤ 10000 → 10%
                if (req.getItemPrice() <= OTHER_PRICE_THRESHOLD) {
                    return req.getItemPrice() * NONSALARIED_OTHER_LOW;
                }
                // R8: non-salaried, other, price > 10000 → 5%
                else {
                    return req.getItemPrice() * NONSALARIED_OTHER_HIGH;
                }

            } else {
                // salaried
                // R9: salaried, other, non-regular → $0.00 (policy silent)
                return 0.0;
            }
        } else {
            throw new IllegalArgumentException(
                    "Unknown item type: " + req.getItemType());
        }
    }
}
