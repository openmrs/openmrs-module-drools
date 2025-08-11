package org.openmrs.module.drools.calculation;

import org.openmrs.Obs;

public class MatchableObsResult {

    private final Obs obs;
    private final ConceptDatatypeWrapper datatype;
    private Object value;

    public MatchableObsResult(Obs obs, ConceptDatatypeWrapper datatype) {
        this.obs = obs;
        this.datatype = datatype;
        if (obs != null) {
            this.value = CalculationUtils.extractObsValue(obs, datatype);
        }
    }

    public boolean matches(Operator operator, Object other) {
        if (obs == null || value == null || datatype == null) {
            return false;
        }
        if (!operator.getSupportedDatatypes().contains(datatype.getDatatypeCode())) {
            throw new IllegalArgumentException("Operator " + operator + " not supported for datatype " + datatype.getDatatype().getName());
        }

        return operator.apply(value, other, datatype);
    }

    public Obs getObs() {
        return obs;
    }

    public Object getValue() {
        return value;
    }

}
