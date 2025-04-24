package org.openmrs.module.drools.calculation;

import org.openmrs.Obs;
import org.openmrs.Patient;

public interface DroolsCalculationService {

    public Boolean latestNumericObsGreaterOrEqual(Patient patient, String conceptUuid, Double value);

    public Boolean latestNumericObsLessThan(Patient patient, String conceptUuid, Double value);

    public Boolean latestNumericObsEqualTo(Patient patient, String conceptUuid, Double value);

    public Boolean latestCodedObsEqualTo(Patient patient, String conceptUuid, String valueConceptUuid);

    public String getLatestObsValueText(Patient patient, String conceptUuid);

    public Obs getLatestObs(Patient patient, String conceptUuid);

    public Boolean isInProgram(Patient patient, String programUuid);

    public Boolean isInProgramState(Patient patient, String stateConceptUuid);

    public Boolean hasActiveConditionCoded(Patient patient, String conditionConceptUuid);

    public Boolean hasEncounterRecord(Patient patient, String encounterTypeUuid);
}
