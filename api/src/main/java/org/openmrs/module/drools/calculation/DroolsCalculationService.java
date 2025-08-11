package org.openmrs.module.drools.calculation;

import org.openmrs.Obs;
import org.openmrs.Patient;

import java.util.Date;

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


    /**
     * Retrieves the most recent observation for the given patient and concept that matches
     * the specified date constraint.
     * <p>
     * The method constructs a date range using the provided {@link Operator} (e.g., {@code LT}, {@code GTE}),
     * and filters observations by their {@code obsDatetime}. For example:
     * <ul>
     *   <li>{@code LT} sets an exclusive upper bound (obsDatetime &lt; date)</li>
     *   <li>{@code LTE} includes the end of the provided day</li>
     *   <li>{@code GT} sets an exclusive lower bound (obsDatetime &gt; date)</li>
     *   <li>{@code GTE} includes the start of the provided day</li>
     *   <li>{@code EQUALS} includes observations occurring on the same calendar day</li>
     * </ul>
     * The most recent matching observation within that range is returned as a {@link MatchableObsResult},
     * allowing value-based assertions using methods like {@code matches(Operator.GT, 5.0)}.
     *
     * @param patient      the patient whose observations are to be evaluated
     * @param conceptRef   the concept UUID or reference to match observations against
     * @param dateOperator the temporal operator to constrain observation dates
     * @param date         the reference date used with the operator
     * @return a {@link MatchableObsResult} wrapping the most recent matching observation, or a null result if none match
     *
     * @throws IllegalArgumentException if the provided operator is not supported for date-based filtering
     *
     * @see MatchableObsResult#matches(Operator, Object)
     * @see Operator
     */
    public MatchableObsResult checkObs(Patient patient, String conceptRef, Operator dateOperator, Date date);

    public Obs getLatestObs(Patient patient, String conceptUuid);

    public Boolean isInProgram(Patient patient, String programUuid);

    public Boolean isInProgramState(Patient patient, String stateConceptUuid);

    public Boolean hasActiveConditionCoded(Patient patient, String conditionConceptUuid);

    public Boolean hasEncounterRecord(Patient patient, String encounterTypeUuid);

}
