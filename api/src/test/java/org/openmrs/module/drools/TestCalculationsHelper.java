package org.openmrs.module.drools;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCalculationsHelper {

    public Boolean hasNumericObsGreaterOrEqual(Patient patient, Concept concept, Double value) {
        List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
        if (obsList == null || obsList.isEmpty()) {
            return false;
        }
        return obsList.stream().anyMatch(obs -> obs.getValueNumeric() >= value);
    }

    public Boolean hasNumericObsLessThan(Patient patient, Concept concept, Double value) {
        List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
        if (obsList == null || obsList.isEmpty()) {
            return false;
        }
        return obsList.stream().anyMatch(obs -> obs.getValueNumeric() < value);
    }

}
