package com.stockle.test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.stockle.ui.DashboardController;

public class marketStatus_Test {
   @Test
    void closedBeforeOpen() {
        ZonedDateTime et = ZonedDateTime.of(2026, 5, 4, 9, 0, 0, 0, ZoneId.of("America/New_York"));
        assertFalse(DashboardController.isMarketOpenAtEt(et));
    }

    @Test
    void marketOpenDuringSession() {
        ZonedDateTime et = ZonedDateTime.of(2026, 5, 4, 10, 0, 0, 0, ZoneId.of("America/New_York"));
        assertTrue(DashboardController.isMarketOpenAtEt(et));
    }

    @Test
    void closedAfterClose() {
        ZonedDateTime et = ZonedDateTime.of(2026, 5, 4, 16, 1, 0, 0, ZoneId.of("America/New_York"));
        assertFalse(DashboardController.isMarketOpenAtEt(et));
    }

    @Test
    void closedOnWeekend() {
        ZonedDateTime et = ZonedDateTime.of(2026, 5, 3, 11, 0, 0, 0, ZoneId.of("America/New_York")); // Sunday
        assertFalse(DashboardController.isMarketOpenAtEt(et));
    } 
}
