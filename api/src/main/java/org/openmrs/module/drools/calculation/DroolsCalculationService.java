package org.openmrs.module.drools.calculation;

import org.openmrs.Obs;
import org.openmrs.Patient;

public interface DroolsCalculationService {

    /**
     * Evaluates the most recent observation for a patient against a specified condition.
     *
     * <p>This method retrieves the most recent observation for the given patient and concept,
     * then applies the specified operator to compare the observation's value against the
     * provided comparison value.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Check if most recent weight is greater than 70kg
     * checkMostRecentObs(patient, "CIEL:5089", Operator.GREATER_THAN, 70.0);
     *
     * // Check if most recent visit date was before today
     * checkMostRecentObs(patient, "CIEL:123", Operator.LESS_THAN, DateUtils.today());
     *
     * // Check if most recent diagnosis equals a specific concept
     * checkMostRecentObs(patient, "CIEL:1284", Operator.EQUALS, "CIEL:5622");
     *
     * @param patient the patient whose observations will be evaluated; must not be null
     * @param conceptRef the concept reference (UUID or mapping)
     * @param operator the comparison operator to apply when evaluating the observation value;
     *                 must not be null and must be compatible with the observation's data type
     * @param value the comparison value to evaluate against; type must be compatible with both
     *              the operator and the observation's data type.
     * @return {@code true} if the observation satisfies the specified condition.
     */
    public Boolean checkMostRecentObs(Patient patient, String conceptRef, Operator operator, Object value);

    public Obs getLatestObs(Patient patient, String conceptUuid);

    public Boolean isInProgram(Patient patient, String programUuid);

    public Boolean isInProgramState(Patient patient, String stateConceptUuid);

    public Boolean hasActiveConditionCoded(Patient patient, String conditionConceptUuid);

    public Boolean hasEncounterRecord(Patient patient, String encounterTypeUuid);

}
