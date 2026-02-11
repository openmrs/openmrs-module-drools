package org.openmrs.module.drools.calculation;

import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DroolsCalculationServiceImp implements DroolsCalculationService {

    @Autowired
    ObsService obsService;

    @Autowired
    ProgramWorkflowService programWorkflowService;

    @Override
    public Boolean checkMostRecentObs(Patient patient, String conceptRef, Operator operator, Object value) {
        Obs obsValue = getLatestObs(patient, conceptRef);

        if (obsValue == null) {
            return false;
        }
        Concept concept = obsValue.getConcept();
        ConceptDatatypeWrapper datatype = new ConceptDatatypeWrapper(concept.getDatatype());
        if (!operator.getSupportedDatatypes().contains(datatype.getDatatypeCode())) {
            throw new IllegalArgumentException(
                    "Operator " + operator + " not supported for datatype " + datatype.getDatatype().getName());
        }
        Object refinedValue = CalculationUtils.extractObsValue((Obs) obsValue, datatype);
        return operator.apply(refinedValue, value, datatype);
    }

    @Override
    public MatchableObsResult checkObs(Patient patient, String conceptRef, Operator dateOperator, Date date) {
        Date fromDate = null;
        Date toDate = null;
        if (dateOperator == null) {
            dateOperator = Operator.EQUALS;
        }
        Concept concept = CalculationUtils.getConcept(conceptRef);

        switch (dateOperator) {
            case LT:
                toDate = new Date(date.getTime() - 1);
                break;
            case LTE:
                toDate = OpenmrsUtil.getLastMomentOfDay(date);
                break;
            case GT:
                fromDate = new Date(date.getTime() + 1);
                break;
            case GTE:
                fromDate = OpenmrsUtil.firstSecondOfDay(date);
                break;
            case EQUALS:
                fromDate = OpenmrsUtil.firstSecondOfDay(date);
                toDate = OpenmrsUtil.getLastMomentOfDay(date);
                break;
            default:
                throw new IllegalArgumentException("Unsupported date operator: " + dateOperator);
        }

        List<Obs> obsList = Context.getObsService().getObservations(
                Collections.singletonList(patient),
                null,
                Collections.singletonList(concept),
                null,
                null,
                null,
                Collections.singletonList("obsDatetime"),
                1,
                null,
                fromDate,
                toDate,
                false);
        return new MatchableObsResult(obsList.isEmpty() ? null : obsList.get(0),
                new ConceptDatatypeWrapper(concept.getDatatype()));
    }

    @Override
    public Obs getLatestObs(Patient patient, String conceptRef) {
        Concept concept = CalculationUtils.getConcept(conceptRef);
        List<Obs> obsList = obsService.getObservations(Arrays.asList(patient.getPerson()), null,
                Arrays.asList(concept), null, null, null, Arrays.asList("obsDatetime"), 1, null, null, null, false);
        if (obsList.isEmpty()) {
            return null;
        }
        return obsList.get(0);
    }

    @Override
    public Boolean isInProgram(Patient patient, String programUuid) {
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
        EncounterType encounterType = getEncounterType(encounterTypeUuid);

        if (encounterType == null) {
            throw new IllegalArgumentException("Encounter type not found for uuid: " + encounterTypeUuid);
        }
        EncounterSearchCriteria criteria = new EncounterSearchCriteria(patient, null, null, null, null, null,
                Collections.singletonList(encounterType), null, null, null, false);
        return !encounterService.getEncounters(criteria).isEmpty();
    }

    @Override
    public Boolean hasEncounter(Patient patient, String encounterTypeUuid, Date from, Date to) {
        EncounterService encounterService = Context.getEncounterService();
        EncounterType encounterType = getEncounterType(encounterTypeUuid);

        if (encounterType == null) {
            throw new IllegalArgumentException("Encounter type not found for uuid: " + encounterTypeUuid);
        }
        EncounterSearchCriteria criteria = new EncounterSearchCriteria(patient, null, from, to, null, null,
                Collections.singletonList(encounterType), null, null, null, false);
        return !encounterService.getEncounters(criteria).isEmpty();
    }

    private EncounterType getEncounterType(String encounterTypeRef) {
        EncounterService encounterService = Context.getEncounterService();
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTypeRef);
        if (encounterType == null) {
            encounterType = encounterService.getEncounterType(encounterTypeRef);
        }
        return encounterType;
    }
}
