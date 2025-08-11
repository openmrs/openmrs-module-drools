package org.openmrs.module.drools.patientflags;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DroolsFlagEvaluatorTest extends BaseModuleContextSensitiveTest {

    @Autowired
    DroolsFlagEvaluator droolsFlagEvaluator;

    DroolsCalculationService calculationService;

    private long obsTimeOffset = 0;

    private Flag sepsisFlag;
    private Patient patient;

    private final Integer TEMPERATURE_CONCEPT_ID = 105, PULSE_CONCEPT_ID = 104, RR_CONCEPT_ID = 103, SYSTOLIC_CONCEPT_ID = 100;

    @Before
    public void setup() throws Exception {
        executeDataSet("org/openmrs/module/drools/include/DroolsFlagEvaluatorTest-dataset.xml");
        if (sepsisFlag == null) {
            sepsisFlag = createSepsisFlag();
        }
        if (patient == null) {
            patient = Context.getPatientService().getPatient(200);
        }
        calculationService = Context.getRegisteredComponents(DroolsCalculationService.class)
                .get(0);
    }

    @Test
    public void eval_shouldEvaluatePatientSepsisFlag() {
        Map<Object, Object> contextMap = new HashMap<>();
        boolean flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertFalse(flagged);

        // provide abnormal values
        saveObs(createObs(patient, TEMPERATURE_CONCEPT_ID, 39.0));
        saveObs(createObs(patient, PULSE_CONCEPT_ID, 100.0));
        saveObs(createObs(patient, RR_CONCEPT_ID, 30.0));

        flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertTrue(flagged);
        assertThat(extractFlagMessage(contextMap, patient), is("Sepsis Warning"));

        // provide a normal value for RR to end up with 2 abnormal values (temperature & pulse)
        saveObs(createObs(patient, RR_CONCEPT_ID, 15.0));
        // clear context
        contextMap = new HashMap<>();

        flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertTrue(flagged);
        assertThat(extractFlagMessage(contextMap, patient), is("Sepsis Warning"));

        // provide a normal value for pulse to end up with 1 abnormal value (temperature)
        saveObs(createObs(patient, PULSE_CONCEPT_ID, 80.0));

        // clear context
        contextMap = new HashMap<>();

        flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertFalse(flagged);

        // provide an abnormal value for Systolic BP and RR
        saveObs(createObs(patient, SYSTOLIC_CONCEPT_ID, 80.0));
        saveObs(createObs(patient, RR_CONCEPT_ID, 30.0));

        // clear context
        contextMap = new HashMap<>();

        flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertTrue(flagged);
        assertThat(extractFlagMessage(contextMap, patient), is("Severe Sepsis"));

        // provide a normal value for temperature so that we end up with two abnormal values (RR & Systolic BP)
        saveObs(createObs(patient, TEMPERATURE_CONCEPT_ID, 36.0));
        saveObs(createObs(patient, SYSTOLIC_CONCEPT_ID, 100.0));
        // clear context
        contextMap = new HashMap<>();

        flagged = droolsFlagEvaluator.eval(sepsisFlag, patient, contextMap);
        assertFalse(flagged);
    }

    private Flag createSepsisFlag() {
        Flag flag = new Flag("Test Sepsis", "{\n" +
                "  \"session\": \"Test Sepsis\",\n" +
                "  \"rules\": [],\n" +
                "  \"agendaGroup\": \"test_sepsis\"\n" +
                "}", "Sepsis");
        flag.setEvaluator("org.openmrs.module.drools.patientflags.DroolsFlagEvaluator");
        return flag;
    }

    private Obs createObs(Patient patient, Integer conceptId, Double value) {
        Obs obs = new Obs();
        obs.setPerson(patient);
        obs.setConcept(Context.getConceptService().getConcept(conceptId));
        obs.setValueNumeric(value);
        obs.setLocation(new Location(1));
        obs.setObsDatetime(new Date(System.currentTimeMillis() + (obsTimeOffset * 1000)));
        obsTimeOffset++;
        return obs;
    }

    private void saveObs(Obs obs) {
        Context.getObsService().saveObs(obs, null);
    }

    private String extractFlagMessage(Map<Object, Object> contextMap, Patient patient) {
        List<String> messages = (List<String>) contextMap.get(patient.getPatientId());
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return messages.get(0);
    }
}
