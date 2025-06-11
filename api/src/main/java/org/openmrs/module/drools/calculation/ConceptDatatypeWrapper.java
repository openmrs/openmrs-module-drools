package org.openmrs.module.drools.calculation;

import org.openmrs.ConceptDatatype;

public class ConceptDatatypeWrapper {

    public ConceptDatatype datatype;

    public ConceptDatatypeWrapper(ConceptDatatype datatype) {
        this.datatype = datatype;
    }

    public String getDatatypeCode() {
        if (datatype.isDate()) {
            return ConceptDatatype.DATE;
        } else if (datatype.isDateTime()) {
            return ConceptDatatype.DATETIME;
        } else if (datatype.isTime()) {
            return ConceptDatatype.TIME;
        } else if (datatype.isCoded()) {
            return ConceptDatatype.CODED;
        } else if (datatype.isBoolean()) {
            return ConceptDatatype.BOOLEAN;
        } else if (datatype.isNumeric()) {
            return ConceptDatatype.NUMERIC;
        } else if (datatype.isText()) {
            return ConceptDatatype.TEXT;
        } else {
            return "UNSUPPORTED";
        }
    }

    public ConceptDatatype getDatatype() {
        return datatype;
    }

    public void setDatatype(ConceptDatatype datatype) {
        this.datatype = datatype;
    }
}
