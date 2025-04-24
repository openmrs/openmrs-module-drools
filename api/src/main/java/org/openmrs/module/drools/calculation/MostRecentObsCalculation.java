package org.openmrs.module.drools.calculation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.EvaluationInstanceData;
import org.openmrs.calculation.InvalidParameterValueException;
import org.openmrs.calculation.patient.PatientAtATimeCalculation;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.calculation.result.SimpleResult;

public class MostRecentObsCalculation extends PatientAtATimeCalculation {

    /**
     * Stores pre-processed data
     */
    public class PatientObsMap extends HashMap<Integer, Obs> implements EvaluationInstanceData {
        private static final long serialVersionUID = 1L;

    }

    @Override
    public EvaluationInstanceData preprocess(Collection<Integer> cohort, Map<String, Object> params,
            PatientCalculationContext context) {

        // TODO: use context cache

        PatientObsMap data = new PatientObsMap();
        Concept concept = (Concept) params.get("concept");
        if (concept == null) {
            throw new InvalidParameterValueException("concept cannot be null");
        }
        for (Integer patientId : cohort) {
            List<Obs> obsList = Context.getObsService().getObservations(Arrays.asList(new Person(patientId)), null,
                    Arrays.asList(concept), null, null, null, null, 1, null, null, null, false);
            data.put(patientId, obsList.get(0));
        }
        return data;
    }

    @Override
    public CalculationResult evaluateForPatient(EvaluationInstanceData instanceData, Integer patientId,
            Map<String, Object> params,
            PatientCalculationContext context) {
        PatientObsMap data = (PatientObsMap) instanceData;
        CalculationResult r = null;
        Obs obs = data.get(patientId);

        if ((boolean) params.getOrDefault("returnObsObject", false)) {
            return new SimpleResult(obs, this);
        }
        if (obs != null) {
            ConceptDatatype datatype = obs.getConcept().getDatatype();

            if (datatype.isCoded() && obs.getValueCoded() != null) {
                r = new SimpleResult(obs.getValueCoded(), this);
            } else if (datatype.isText() && obs.getValueText() != null) {
                r = new SimpleResult(obs.getValueText(), this);
            } else if (datatype.isNumeric() && obs.getValueNumeric() != null) {
                r = new SimpleResult(obs.getValueNumeric(), this);
            } else {
                // FIXME: Add support for other datatypes
                throw new UnsupportedOperationException();
            }
        }
        return r;

    }

}
