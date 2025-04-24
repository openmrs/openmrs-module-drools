package org.openmrs.module.drools.calculation;

import java.util.Collections;
import java.util.HashMap;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.parameter.EncounterSearchCriteria;

public class DroolsCalculationServiceImp implements DroolsCalculationService {

    private MostRecentObsCalculation obsCalculation = new MostRecentObsCalculation();

    private PatientCalculationService calculationService;

    private PatientCalculationContext context;

    @Override
    public Boolean latestNumericObsGreaterOrEqual(Patient patient, String conceptUuid, Double value) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                Collections.singletonMap("concept", getConcept(conceptUuid)), getContext());
        Double obsValue = (Double) result.getValue();
        return obsValue != null && obsValue >= value;
    }

    @Override
    public Boolean latestNumericObsLessThan(Patient patient, String conceptUuid, Double value) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                Collections.singletonMap("concept", getConcept(conceptUuid)), getContext());
        Double obsValue = (Double) result.getValue();
        return obsValue != null && obsValue < value;
    }

    @Override
    public Boolean latestNumericObsEqualTo(Patient patient, String conceptUuid, Double value) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                Collections.singletonMap("concept", getConcept(conceptUuid)), getContext());
        Double obsValue = (Double) result.getValue();
        return obsValue != null && obsValue == value;
    }

    @Override
    public Boolean latestCodedObsEqualTo(Patient patient, String conceptUuid, String valueConceptUuid) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                Collections.singletonMap("concept", getConcept(conceptUuid)), getContext());
        Concept obsValue = (Concept) result.getValue();
        return obsValue != null && obsValue.getUuid().equals(valueConceptUuid);
    }

    @Override
    public String getLatestObsValueText(Patient patient, String conceptUuid) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                Collections.singletonMap("concept", getConcept(conceptUuid)), getContext());
        return (String) result.getValue();
    }

    @Override
    public Obs getLatestObs(Patient patient, String conceptUuid) {
        CalculationResult result = getCalculationService().evaluate(patient.getId(), obsCalculation,
                new HashMap<String, Object>() {
                    {
                        put("concept", getConcept(conceptUuid));
                        put("returnObsObject", true);
                    }
                }, getContext());
        return (Obs) result.getValue();
    }

    @Override
    public Boolean isInProgram(Patient patient, String programUuid) {
        ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();
        Program program = programWorkflowService.getProgramByUuid(programUuid);
        if (program == null) {
            throw new IllegalArgumentException("Program not found for uuid: " + programUuid);
        }
        return programWorkflowService.getPatientPrograms(patient, program, null,
                null, null, null, false).stream().anyMatch(p -> p.getActive());

    }

    @Override
    public Boolean isInProgramState(Patient patient, String stateConceptUuid) {
        // TODO provide implementation
        throw new UnsupportedOperationException("Unimplemented method 'isInProgramState'");
    }

    @Override
    public Boolean hasActiveConditionCoded(Patient patient, String conditionConceptUuid) {
        // TODO provide implementation
        throw new UnsupportedOperationException("Unimplemented method 'hasActiveConditionCoded'");
    }

    @Override
    public Boolean hasEncounterRecord(Patient patient, String encounterTypeUuid) {
        EncounterService encounterService = Context.getEncounterService();
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTypeUuid);
        if (encounterType == null) {
            throw new IllegalArgumentException("Encounter type not found for uuid: " + encounterTypeUuid);
        }
        EncounterSearchCriteria criteria = new EncounterSearchCriteria(patient, null, null, null, null, null,
                Collections.singletonList(encounterType), null, null, null, false);
        return !encounterService.getEncounters(criteria).isEmpty();
    }

    private PatientCalculationService getCalculationService() {
        if (calculationService == null) {
            calculationService = Context.getService(PatientCalculationService.class);
        }
        return calculationService;
    }

    private PatientCalculationContext getContext() {
        if (context == null) {
            context = getCalculationService().createCalculationContext();
        }
        return context;
    }

    private Concept getConcept(String conceptUuid) {
        // TODO: cache concepts
        Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
        if (concept == null) {
            throw new IllegalArgumentException("Concept not found for uuid: " + conceptUuid);
        }
        return concept;
    }

}
