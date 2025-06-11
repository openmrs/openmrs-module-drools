package org.openmrs.module.drools.calculation;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.utils.DroolsDateUtils;
import java.text.ParseException;

public class CalculationUtils {

    public static Object refineRhsOperand(Object operand, ConceptDatatype datatype) {
        if (datatype.isText() || datatype.isNumeric() || datatype.isBoolean()) {
            // we assume this is defined in its primitive/boxed form
            return operand;
        }
        // TODO: handle LocalDate instances
        if (datatype.containsDate() && operand instanceof String) {
            // parse date
            try {
                return DroolsDateUtils.parseDate((String) operand);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (datatype.isTime() && operand instanceof String) {
            // parse to LocalTime
            return DroolsDateUtils.parseTime((String) operand);
        }
        if (datatype.isCoded() && operand instanceof String) {
            // resolve concept
            Concept concept = getConcept((String) operand);
            if (concept == null) {
                throw new IllegalArgumentException("Couldn't resolve concept with ref: " + operand);
            }
            return concept;
        }
        return operand;
    }

    public static Concept getConcept(String conceptUuidOrMapping) {
        ConceptService conceptService = Context.getConceptService();
        if (StringUtils.isBlank(conceptUuidOrMapping)) {
            throw new IllegalArgumentException("Concept ref can't be blank");
        }

        if (conceptUuidOrMapping.indexOf(":") > 0) {
            String [] parts = conceptUuidOrMapping.split(":");
            return conceptService.getConceptByMapping(parts[1], parts[0]);
        }
        return conceptService.getConceptByUuid(conceptUuidOrMapping);
    }

    public static Object extractObsValue(Obs obs, ConceptDatatypeWrapper datatype){
        if (obs == null) {
            return null;
        }

        switch (datatype.getDatatypeCode()) {
            case ConceptDatatype.BOOLEAN:
                return obs.getValueBoolean();
            case ConceptDatatype.CODED:
                return obs.getValueCoded();
            case ConceptDatatype.TEXT:
                return obs.getValueText();
            case ConceptDatatype.NUMERIC:
                return obs.getValueNumeric();
            case ConceptDatatype.DATE:
                return obs.getValueDate();
            case ConceptDatatype.DATETIME:
                return obs.getValueDatetime();
            case ConceptDatatype.TIME:
                return obs.getValueTime();
            default:
                throw new IllegalArgumentException("Unsupported concept datatype: " + datatype.getDatatypeCode());
        }
    }
}
