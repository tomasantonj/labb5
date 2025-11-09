package ltu;

import static org.junit.Assert.*;

import org.junit.Test;

public class PaymentTest
{

    private PaymentImpl payment = new PaymentImpl();

    // Helper to create a calendar for a specific date
    private java.util.Calendar getCalendar(int year, int month, int day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, month - 1); // Calendar months are 0-based
        cal.set(java.util.Calendar.DAY_OF_MONTH, day);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal;
    }

    // [201] The student must be studying at least half time to receive any subsidiary.
    @Test
    public void testNoSubsidiaryIfLessThanHalfTime() {
        double subsidiary = payment.calculateSubsidiary(0.4); // 40% study pace
        assertEquals(0.0, subsidiary, 0.001);
    }

    // [202] A student studying less than full time is entitled to 50% subsidiary.
    @Test
    public void testHalfSubsidiaryIfHalfTime() {
        double subsidiary = payment.calculateSubsidiary(0.5); // 50% study pace
        assertEquals(0.5, subsidiary, 0.001);
    }

    @Test
    public void testHalfSubsidiaryIfMoreThanHalfTimeButLessThanFullTime() {
        double subsidiary = payment.calculateSubsidiary(0.75); // 75% study pace
        assertEquals(0.5, subsidiary, 0.001);
    }

    // [203] A student studying full time is entitled to 100% subsidiary.
    @Test
    public void testFullSubsidiaryIfFullTime() {
        double subsidiary = payment.calculateSubsidiary(1.0); // 100% study pace
        assertEquals(1.0, subsidiary, 0.001);
    }

    // [506] Student loans and subsidiary is paid on the last weekday (Monday to Friday) every month.
    @Test
    public void testPaymentDatesSpring2016() {
        int[][] expectedDates = {
            {2016, 1, 29}, // Jan 29, 2016 (Friday)
            {2016, 2, 29}, // Feb 29, 2016 (Monday)
            {2016, 3, 31}, // Mar 31, 2016 (Thursday)
            {2016, 4, 29}, // Apr 29, 2016 (Friday)
            {2016, 5, 31}, // May 31, 2016 (Tuesday)
            {2016, 6, 30}  // Jun 30, 2016 (Thursday)
        };
        for (int[] date : expectedDates) {
            java.util.Calendar cal = getCalendar(date[0], date[1], 1);
            java.util.Calendar paymentDate = payment.getPaymentDate(cal);
            assertEquals(date[0], paymentDate.get(java.util.Calendar.YEAR));
            assertEquals(date[1] - 1, paymentDate.get(java.util.Calendar.MONTH));
            assertEquals(date[2], paymentDate.get(java.util.Calendar.DAY_OF_MONTH));
            // Ensure it's a weekday
            int dayOfWeek = paymentDate.get(java.util.Calendar.DAY_OF_WEEK);
            assertTrue(dayOfWeek >= java.util.Calendar.MONDAY && dayOfWeek <= java.util.Calendar.FRIDAY);
        }
    }

}
