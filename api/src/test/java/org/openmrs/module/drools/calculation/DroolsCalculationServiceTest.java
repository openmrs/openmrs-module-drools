package org.openmrs.module.drools.calculation;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;
import static org.openmrs.module.drools.utils.DroolsDateUtils.weeksAgo;

// TODO: Use org.openmrs.test.jupiter.BaseModuleContextSensitiveTest as base class instead
public class DroolsCalculationServiceTest extends BaseModuleContextSensitiveTest {
    private static final String TEST_OBS_XML = "org/openmrs/module/drools/testdata/DroolsCalculationServiceTest-dataset.xml";

    @Autowired
    private DroolsCalculationService calculationService;

    @Autowired
    private ConceptService conceptService;

    private Patient patient;
    private String conceptRef;
    private Date jan1, jan2, dec31, sept22;

    @Before
    public void setUp() throws Exception {
        executeDataSet(TEST_OBS_XML);
        patient = new Patient(1);
        conceptRef = "1001";
        jan1 = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
        jan2 = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-02");
        sept22 = new SimpleDateFormat("yyyy-MM-dd").parse("2023-09-22");
        dec31 = new SimpleDateFormat("yyyy-MM-dd").parse("2023-12-31");
    }

    @Test
    public void checkMostRecentObs_shouldSupportOperators() {
        assertTrue(calculationService.checkMostRecentObs(patient, conceptRef, Operator.LT, 8));
        assertFalse(calculationService.checkMostRecentObs(patient, conceptRef, Operator.LT, 7));
        assertTrue(calculationService.checkMostRecentObs(patient, conceptRef, Operator.LTE, 7.0));
        assertTrue(calculationService.checkMostRecentObs(patient, conceptRef, Operator.GT, 4.0));
        assertFalse(calculationService.checkMostRecentObs(patient, conceptRef, Operator.GT, 7.0));
        assertTrue(calculationService.checkMostRecentObs(patient, conceptRef, Operator.GTE, 7.0));
        assertTrue(calculationService.checkMostRecentObs(patient, conceptRef, Operator.EQUALS, 7.0));
        assertFalse(calculationService.checkMostRecentObs(patient, conceptRef, Operator.EQUALS, 8));
    }

    @Test
    public void checkObs_shouldSupportOperatorLT() {
        MatchableObsResult result = calculationService.checkObs(patient, conceptRef, Operator.LT, jan1);
        assertNotNull(result);
        assertTrue(result.matches(Operator.EQUALS, 3.0));
    }

    @Test
    public void checkObs_shouldSupportOperatorLTE() {
        MatchableObsResult result = calculationService.checkObs(patient, conceptRef, Operator.LTE, jan2);
        assertNotNull(result);
        // it returns the latest obs
        assertTrue(result.matches(Operator.EQUALS, 7.0));

        // replay
        result = calculationService.checkObs(patient, conceptRef, Operator.LTE, sept22);
        assertTrue(result.matches(Operator.EQUALS, 4.0));
    }

    @Test
    public void checkObs_shouldSupportOperatorEQUALS() {
        MatchableObsResult result = calculationService.checkObs(patient, conceptRef, Operator.EQUALS, jan1);
        assertNotNull(result);
        assertTrue(result.matches(Operator.EQUALS, 5.0));
        assertFalse(result.matches(Operator.EQUALS, 3.0));
    }

    @Test
    public void checkObs_shouldSupportOperatorGT() {
        MatchableObsResult result = calculationService.checkObs(patient, conceptRef, Operator.GT, dec31);
        assertNotNull(result);
        // returns the latest obs
        assertTrue(result.matches(Operator.EQUALS, 7.0));
    }

    @Test
    public void checkObs_shouldSupportOperatorGTE() {
        MatchableObsResult result = calculationService.checkObs(patient, conceptRef, Operator.GTE, jan1);
        assertNotNull(result);
        // returns the latest obs
        assertTrue(result.matches(Operator.EQUALS, 7.0));
    }

    public void checkObs_shouldQueryObsCaptured2WeeksAgo() {
        calculationService.checkObs(patient, "CIEL:123", null, weeksAgo(2)).matches(Operator.EQUALS, 5);
    }

}
