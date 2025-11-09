package ltu;

import static org.junit.Assert.*;

import org.junit.Test;

public class PaymentTest
{





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
        // Should be half subsidiary (1396 SEK from PaymentImpl)
        assertEquals(1396, amount);
    }

    @Test
    public void testHalfSubsidiaryIfMoreThanHalfTimeButLessThanFullTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate = 75 should yield half subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 75, 100);
        // Should be half subsidiary (1396 SEK from PaymentImpl)
        assertEquals(1396, amount);
    }


    // [203] A student studying full time is entitled to 100% subsidiary.
    @Test
    public void testFullSubsidiaryIfFullTime() throws Exception {
        IPayment payment = new PaymentImpl(new CalendarImpl());
        // studyRate = 100 should yield full subsidiary
        int amount = payment.getMonthlyAmount("19960101-1234", 0, 100, 100);
        // Should be full subsidiary (2816 SEK from PaymentImpl)
        assertEquals(2816, amount);
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

}
