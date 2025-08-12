package org.openmrs.module.drools.calculation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CalculationUtils.class)
@PowerMockIgnore({
        "javax.xml.*",
        "org.xml.*",
        "org.w3c.*",
        "org.apache.xerces.*",
        "com.sun.org.apache.xerces.*",
        "org.slf4j.*",
        "org.apache.logging.*"
})
public class OperatorTest {
    private ConceptDatatypeWrapper coded;
    private ConceptDatatypeWrapper numeric;
    private ConceptDatatypeWrapper date;
    private ConceptDatatypeWrapper dateTime;
    private ConceptDatatypeWrapper time;
    private ConceptDatatypeWrapper n_a;

    @Before
    public void setup() {
        coded = createDatatype(ConceptDatatype.CODED_UUID);
        numeric = createDatatype(ConceptDatatype.NUMERIC_UUID);
        date = createDatatype(ConceptDatatype.DATE_UUID);
        dateTime = createDatatype(ConceptDatatype.DATETIME_UUID);
        time = createDatatype(ConceptDatatype.TIME_UUID);
        n_a = createDatatype(ConceptDatatype.N_A_UUID);

    }

    // ==================== EQUALS OPERATOR TESTS ====================

    @Test
    public void equals_shouldSupportCoded() {
        // setup
        PowerMockito.mockStatic(CalculationUtils.class);

        String conceptUuid1 = "0308b8a7-a4a2-47e0-9cfc-a56a65e2b49c";
        String conceptUuid2 = "d98bb8d0-66f2-4aa7-870b-444527562f64";
        Concept c1 = createConcept(conceptUuid1, n_a);
        Concept c2 = createConcept(conceptUuid2, n_a);
        Concept c3 = createConcept(conceptUuid1, n_a);

        when(CalculationUtils.refineRhsOperand(any(), any())).thenCallRealMethod();
        when(CalculationUtils.getConcept("CIEL:645")).thenReturn(c2);

        Assert.assertFalse(Operator.EQUALS.apply(c1, c2, coded));
        Assert.assertTrue(Operator.EQUALS.apply(c1, c3, coded));
        Assert.assertTrue(Operator.EQUALS.apply(c2, "CIEL:645", coded));
    }

    @Test
    public void equals_shouldSupportNumeric() {
        Assert.assertTrue(Operator.EQUALS.apply(10.5, 10.5, numeric));
        Assert.assertTrue(Operator.EQUALS.apply(10, 10.0, numeric));
        Assert.assertFalse(Operator.EQUALS.apply(10.5, 10.6, numeric));
        Assert.assertTrue(Operator.EQUALS.apply(0, 0.0, numeric));
        Assert.assertFalse(Operator.EQUALS.apply(null, 10.5, numeric));
    }

    @Test
    public void equals_shouldSupportDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date1 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date2 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 16, 0, 0, 0);
        Date date3 = cal.getTime();

        Assert.assertTrue(Operator.EQUALS.apply(date1, date2, date));
        Assert.assertTrue(Operator.EQUALS.apply(date1, "2023-01-15", date));
        Assert.assertFalse(Operator.EQUALS.apply(date1, date3, date));
        Assert.assertFalse(Operator.EQUALS.apply(null, date1, date));
    }

    @Test
    public void equals_shouldSupportDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date dateTime1 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 15, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date dateTime2 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 15, 14, 30, 46);
        Date dateTime3 = cal.getTime();

        Assert.assertTrue(Operator.EQUALS.apply(dateTime1, dateTime2, dateTime));
        Assert.assertTrue(Operator.EQUALS.apply(dateTime3, "2023-01-15 14:30:46", dateTime));
        Assert.assertFalse(Operator.EQUALS.apply(dateTime1, dateTime3, dateTime));
    }

    @Test
    public void equals_shouldSupportTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 9, 15, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time1 = cal.getTime();

        cal.set(1970, Calendar.JANUARY, 1, 9, 15, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time2 = cal.getTime();

        cal.set(1970, Calendar.JANUARY, 1, 10, 30, 0);
        Date time3 = cal.getTime();

        Assert.assertTrue(Operator.EQUALS.apply(time1, time2, time));
        Assert.assertTrue(Operator.EQUALS.apply(time1, "09:15", time));
        Assert.assertFalse(Operator.EQUALS.apply(time1, time3, time));
        Assert.assertFalse(Operator.EQUALS.apply(null, time1, time));
    }

    // ==================== LESS_THAN OPERATOR TESTS ====================

    @Test
    public void lessThan_shouldSupportNumeric() {
        Assert.assertTrue(Operator.LT.apply(5.0, 10.0, numeric));
        Assert.assertFalse(Operator.LT.apply(10.0, 5.0, numeric));
        Assert.assertFalse(Operator.LT.apply(10.0, 10.0, numeric));
        Assert.assertTrue(Operator.LT.apply(-5.0, 0.0, numeric));
        Assert.assertTrue(Operator.LT.apply(null, 10.0, numeric));
    }

    @Test
    public void lessThan_shouldSupportDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15);
        Date earlierDate = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 16);
        Date laterDate = cal.getTime();

        Assert.assertTrue(Operator.LT.apply(earlierDate, laterDate, date));
        Assert.assertFalse(Operator.LT.apply(laterDate, earlierDate, date));
        Assert.assertFalse(Operator.LT.apply(earlierDate, earlierDate, date));
    }

    @Test
    public void lessThan_shouldSupportTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 8, 0, 0); // 08:00:00
        cal.set(Calendar.MILLISECOND, 0);
        Date time1 = cal.getTime();

        cal.set(1970, Calendar.JANUARY, 1, 9, 30, 0); // 09:30:00
        Date time2 = cal.getTime();

        cal.set(1970, Calendar.JANUARY, 1, 8, 0, 0); // identical to time1
        Date time3 = cal.getTime();

        Assert.assertTrue(Operator.LT.apply(time1, time2, time));
        Assert.assertFalse(Operator.LT.apply(time2, time1, time));
        Assert.assertFalse(Operator.LT.apply(time1, time3, time));
        Assert.assertTrue(Operator.LT.apply(time1, "09:00", time));
        Assert.assertTrue(Operator.LT.apply(null, time2, time));
    }

    // ==================== LESS_THAN_OR_EQUAL OPERATOR TESTS ====================

    @Test
    public void lessThanOrEqual_shouldSupportNumeric() {
        Assert.assertTrue(Operator.LTE.apply(5.0, 10.0, numeric));
        Assert.assertTrue(Operator.LTE.apply(10.0, 10.0, numeric));
        Assert.assertFalse(Operator.LTE.apply(10.0, 5.0, numeric));
        Assert.assertTrue(Operator.LTE.apply(-5.0, 0.0, numeric));
    }

    @Test
    public void lessThanOrEqual_shouldSupportDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date1 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date2 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 16);
        Date laterDate = cal.getTime();

        Assert.assertTrue(Operator.LTE.apply(date1, date2, date));
        Assert.assertTrue(Operator.LTE.apply(date1, laterDate, date));
        Assert.assertFalse(Operator.LTE.apply(laterDate, date1, date));
    }

    // ==================== GREATER_THAN OPERATOR TESTS ====================

    @Test
    public void greaterThan_shouldSupportNumeric() {
        Assert.assertFalse(Operator.GT.apply(5.0, 10.0, numeric));
        Assert.assertTrue(Operator.GT.apply(10.0, 5.0, numeric));
        Assert.assertFalse(Operator.GT.apply(10.0, 10.0, numeric));
        Assert.assertFalse(Operator.GT.apply(-5.0, 0.0, numeric));
        Assert.assertTrue(Operator.GT.apply(0.0, -5.0, numeric));
    }

    @Test
    public void greaterThan_shouldSupportDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15);
        Date earlierDate = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 16);
        Date laterDate = cal.getTime();

        Assert.assertFalse(Operator.GT.apply(earlierDate, laterDate, date));
        Assert.assertTrue(Operator.GT.apply(laterDate, earlierDate, date));
        Assert.assertFalse(Operator.GT.apply(earlierDate, earlierDate, date));
    }

    // ==================== GREATER_THAN_OR_EQUAL OPERATOR TESTS ====================

    @Test
    public void greaterThanOrEqual_shouldSupportNumeric() {
        Assert.assertFalse(Operator.GTE.apply(5.0, 10.0, numeric));
        Assert.assertTrue(Operator.GTE.apply(10.0, 10.0, numeric));
        Assert.assertTrue(Operator.GTE.apply(10.0, 5.0, numeric));
        Assert.assertTrue(Operator.GTE.apply(0.0, -5.0, numeric));
    }

    @Test
    public void greaterThanOrEqual_shouldSupportDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date1 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date2 = cal.getTime();

        cal.set(2023, Calendar.JANUARY, 16);
        Date laterDate = cal.getTime();

        Assert.assertTrue(Operator.GTE.apply(date1, date2, date));
        Assert.assertFalse(Operator.GTE.apply(date1, laterDate, date));
        Assert.assertTrue(Operator.GTE.apply(laterDate, date1, date));
    }

    private ConceptDatatypeWrapper createDatatype(String Uuid) {
        ConceptDatatype type = new ConceptDatatype();
        type.setUuid(Uuid);
        return new ConceptDatatypeWrapper(type);
    }

    private Concept createConcept(String Uuid, ConceptDatatypeWrapper datatype) {
        Concept concept = new Concept();
        concept.setUuid(Uuid);
        concept.setDatatype(datatype.getDatatype());
        return concept;
    }
}
