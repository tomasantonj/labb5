package ltu;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class PaymentTest
{
    private static final String SPRING_TERM_DATE = "2016-03-15";

    // [IDs: 101] (must be ≥20 → under 20 gets nothing)
    @Test
    public void studentYoungerThanTwentyReceivesNoSupport() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1997010155555", 10000, 100, 100);
        assertEquals(0, amount);
    }

    // [IDs: 101, 501, 502] (turning 20 eligible; verifies full loan & full subsidy amounts)
    @Test
    public void studentTurningTwentyReceivesLoanAndSubsidy() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1996010155555", 10000, 100, 100);
        assertEquals(7088 + 2816, amount);
    }

    // [IDs: 102, 103, 502] (subsidy allowed through year turning 56; loan disallowed ≥47; checks full subsidy amount)
    @Test
    public void studentKeepsSubsidyUntilTurningFiftySeven() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1960010155555", 10000, 100, 100);
        assertEquals(2816, amount);
    }

    // [IDs: 102, 103] (age >56 → no subsidy; loan already disallowed ≥47 → total 0)
    @Test
    public void studentOlderThanFiftySixGetsNoSubsidy() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1959010155555", 10000, 100, 100);
        assertEquals(0, amount);
    }

    // [IDs: 103, 102, 502] (from the year turning 47 → no loan; subsidy still allowed; checks subsidy amount)
    @Test
    public void studentCannotReceiveLoanFromAgeFortySeven() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1969010155555", 10000, 100, 100);
        assertEquals(2816, amount);
    }

    // [IDs: 103, 501, 502] (age <47 → loan allowed; checks full loan + full subsidy amounts)
    @Test
    public void studentYoungerThanFortySevenCanReceiveFullLoan() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1970010155555", 10000, 100, 100);
        assertEquals(7088 + 2816, amount);
    }

    // [IDs: 201] (study pace <50% → no support)
    @Test
    public void studyingLessThanHalfTimeDisqualifiesSupport() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1990010155555", 10000, 25, 100);
        assertEquals(0, amount);
    }

    // [IDs: 201, 503, 504] (≥50% half-time eligible; verifies half loan + half subsidy amounts)
    @Test
    public void studyingHalfTimeGivesHalfSupport() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1990010155555", 10000, 50, 100);
        assertEquals(3564 + 1396, amount);
    }

    // [IDs: 203, 501, 502] (full-time → 100% subsidy; verifies full loan + full subsidy amounts)
    @Test
    public void studyingFullTimeGivesFullSupport() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        int amount = payment.getMonthlyAmount("1990010155555", 10000, 100, 100);
        assertEquals(7088 + 2816, amount);
    }

    // [IDs: 301] (full-time income threshold: 85,813 inclusive; 85,814 disqualifies)
    @Test
    public void fullTimeStudentMustStayBelowIncomeThreshold() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        assertEquals(7088 + 2816, payment.getMonthlyAmount("1990010155555", 85813, 100, 100));
        assertEquals(0, payment.getMonthlyAmount("1990010155555", 85814, 100, 100));
    }

    // [IDs: 302, 503, 504] (part-time income threshold: 128,722 inclusive; 128,723 disqualifies; verifies half amounts)
    @Test
    public void partTimeStudentHasHigherIncomeThreshold() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        assertEquals(3564 + 1396, payment.getMonthlyAmount("1990010155555", 128722, 75, 100));
        assertEquals(0, payment.getMonthlyAmount("1990010155555", 128723, 75, 100));
    }

    // [IDs: 401] (completion ratio must be at least 50%: 50 accepted, 49 rejected)
    @Test
    public void completionRatioMustBeAtLeastFiftyPercent() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        assertEquals(7088 + 2816, payment.getMonthlyAmount("1990010155555", 10000, 100, 50));
        assertEquals(0, payment.getMonthlyAmount("1990010155555", 10000, 100, 49));
    }

    // [IDs: 505, 503, 501] (loan amount is always the full defined amount for eligibility; part-time → 3564, full-time → 7088)
    @Test
    public void loanAmountIsAlwaysFullForEligibleStudents() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        assertEquals(3564, payment.getMonthlyAmount("1990010155555", 10000, 75, 100) - 1396);
        assertEquals(7088, payment.getMonthlyAmount("1990010155555", 10000, 100, 100) - 2816);
    }

    // [IDs: 202, 203, 504, 502] (subsidy scales with study rate: part-time → 1396, full-time → 2816)
    @Test
    public void subsidyAmountMatchesStudyRate() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        assertEquals(1396, payment.getMonthlyAmount("1990010155555", 10000, 75, 100) - 3564);
        assertEquals(2816, payment.getMonthlyAmount("1990010155555", 10000, 100, 100) - 7088);
    }

    // [IDs: 506] (paid on last weekday of January 2016)
    @Test
    public void january2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-01-10");
        assertEquals("20160129", payment.getNextPaymentDay());
    }

    // [IDs: 506] (paid on last weekday of February 2016)
    @Test
    public void february2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-02-10");
        assertEquals("20160229", payment.getNextPaymentDay());
    }

    // [IDs: 506] (paid on last weekday of March 2016)
    @Test
    public void march2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-03-10");
        assertEquals("20160331", payment.getNextPaymentDay());
    }

    // [IDs: 506] (paid on last weekday of April 2016)
    @Test
    public void april2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-04-10");
        assertEquals("20160429", payment.getNextPaymentDay());
    }

    // [IDs: 506] (paid on last weekday of May 2016)
    @Test
    public void may2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-05-10");
        assertEquals("20160531", payment.getNextPaymentDay());
    }

    // [IDs: 506] (paid on last weekday of June 2016)
    @Test
    public void june2016PaysOnLastWeekday() throws Exception
    {
        PaymentImpl payment = createPayment("2016-06-10");
        assertEquals("20160630", payment.getNextPaymentDay());
    }

    // [No-ID] (input validation outside given requirements)
    @Test(expected = IllegalArgumentException.class)
    public void nullPersonIdIsRejected() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        payment.getMonthlyAmount(null, 10000, 100, 100);
    }

    // [No-ID] (input validation outside given requirements)
    @Test(expected = IllegalArgumentException.class)
    public void negativeIncomeIsRejected() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        payment.getMonthlyAmount("1990010155555", -1, 100, 100);
    }

    // [No-ID] (input validation outside given requirements)
    @Test(expected = IllegalArgumentException.class)
    public void invalidPersonIdLengthIsRejected() throws Exception
    {
        PaymentImpl payment = createPayment(SPRING_TERM_DATE);
        payment.getMonthlyAmount("199001015555", 10000, 100, 100);
    }

    private PaymentImpl createPayment(String isoDate) throws Exception
    {
        return new PaymentImpl(new FixedCalendar(isoDate));
    }

    private static class FixedCalendar implements ICalendar
    {
        private final Date date;

        private FixedCalendar(String isoDate)
        {
            try
            {
                this.date = new SimpleDateFormat("yyyy-MM-dd").parse(isoDate);
            } catch (ParseException e)
            {
                throw new IllegalArgumentException("Invalid date: " + isoDate, e);
            }
        }

        @Override
        public Date getDate()
        {
            return date;
        }
    }
}
