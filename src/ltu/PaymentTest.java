package ltu;

import static org.junit.Assert.*;
import org.junit.Test;

public class PaymentTest {
    // [201] The student must be studying at least half time to receive any subsidiary.
    @Test
    public void testNoSubsidiaryIfLessThanHalfTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate < 50 should yield 0 subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 40, 100);
        // Only subsidiary, no loan, so should be 0
        assertEquals(0, amount);
    }
    // [202] A student studying less than full time is entitled to 50% subsidiary.
    @Test
    public void testHalfSubsidiaryIfHalfTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate = 50 should yield half subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 50, 100);
    // Should be half loan (3564) + half subsidiary (1396) = 4960 SEK from PaymentImpl
    assertEquals(4960, amount);
    }

    @Test
    public void testHalfSubsidiaryIfMoreThanHalfTimeButLessThanFullTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate = 75 should yield half subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 75, 100);
    // Should be half loan (3564) + half subsidiary (1396) = 4960 SEK from PaymentImpl
    assertEquals(4960, amount);
    }

    // [203] A student studying full time is entitled to 100% subsidiary.
    @Test
    public void testFullSubsidiaryIfFullTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate = 100 should yield full subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 100, 100);
    // Should be full loan (7088) + full subsidiary (2816) = 9904 SEK from PaymentImpl
    assertEquals(9904, amount);
    }

    // [506] Student loans and subsidiary is paid on the last weekday (Monday to Friday) every month.
    @Test
    public void testPaymentDatesSpring2016() throws Exception {
        int[][] expectedDates = {
            {2016, 1, 29}, // Jan 29, 2016 (Friday)
            {2016, 2, 29}, // Feb 29, 2016 (Monday)
            {2016, 3, 31}, // Mar 31, 2016 (Thursday)
            {2016, 4, 29}, // Apr 29, 2016 (Friday)
            {2016, 5, 31}, // May 31, 2016 (Tuesday)
            {2016, 6, 30}  // Jun 30, 2016 (Thursday)
        };
        for (int[] date : expectedDates) {
            // Custom calendar for each month
            ICalendar cal = new ICalendar() {
                @Override
                public java.util.Date getDate() {
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.set(java.util.Calendar.YEAR, date[0]);
                    c.set(java.util.Calendar.MONTH, date[1] - 1);
                    c.set(java.util.Calendar.DAY_OF_MONTH, 1);
                    c.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    c.set(java.util.Calendar.MINUTE, 0);
                    c.set(java.util.Calendar.SECOND, 0);
                    c.set(java.util.Calendar.MILLISECOND, 0);
                    return c.getTime();
                }
            };
            IPayment payment = new PaymentImpl(cal);
            String paymentDay = payment.getNextPaymentDay();
            String expected = String.format("%04d%02d%02d", date[0], date[1], date[2]);
            assertEquals(expected, paymentDay);
        }
    }

    // Edge case: studyRate just below 50 (should get 0)
    @Test
    public void testNoSubsidiaryIfJustBelowHalfTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 49, 100);
        assertEquals(0, amount);
    }

    // Edge case: completionRatio just below 50 (should get 0)
    @Test
    public void testNoSubsidiaryIfLowCompletionRatio() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 100, 49);
        assertEquals(0, amount);
    }

    // Edge case: age under 20 (should get 0)
    @Test
    public void testNoSubsidiaryIfTooYoung() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("20100101-1234", 0, 100, 100);
        assertEquals(0, amount);
    }

    // Edge case: age over 56 (should get 0 subsidiary)
    @Test
    public void testNoSubsidiaryIfTooOld() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("19500101-1234", 0, 100, 100);
        assertEquals(0, amount);
    }

    // Edge case: age over 47 (should get 0 loan, but still get subsidiary if eligible)
    @Test
    public void testNoLoanIfTooOldButSubsidiaryPossible() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // Age 48, full time, eligible for subsidiary only
        int amount = payment.getMonthlyAmount("19770101-1234", 0, 100, 100);
        // Should be only full subsidiary (2816)
        assertEquals(2816, amount);
    }

    // Edge case: income just above fulltime threshold (should get 0 loan and 0 subsidiary)
    @Test
    public void testNoSupportIfIncomeTooHighFullTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("19960101-1234", 85814, 100, 100); // 1 above FULLTIME_INCOME
        assertEquals(0, amount);
    }

    // Edge case: income just above parttime threshold (should get 0 loan and 0 subsidiary)
    @Test
    public void testNoSupportIfIncomeTooHighPartTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        int amount = payment.getMonthlyAmount("19960101-1234", 128723, 75, 100); // 1 above PARTTIME_INCOME
        assertEquals(0, amount);
    }

    // Invalid input: negative income
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIncomeThrows() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        payment.getMonthlyAmount("19960101-1234", -1, 100, 100);
    }

    // Invalid input: negative studyRate
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeStudyRateThrows() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        payment.getMonthlyAmount("19960101-1234", 0, -1, 100);
    }

    // Invalid input: negative completionRatio
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCompletionRatioThrows() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        payment.getMonthlyAmount("19960101-1234", 0, 100, -1);
    }

    // Invalid input: null personId
    @Test(expected = IllegalArgumentException.class)
    public void testNullPersonIdThrows() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        payment.getMonthlyAmount(null, 0, 100, 100);
    }

    // Invalid input: malformed personId
    @Test(expected = IllegalArgumentException.class)
    public void testMalformedPersonIdThrows() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        payment.getMonthlyAmount("19960101", 0, 100, 100);
    }
}