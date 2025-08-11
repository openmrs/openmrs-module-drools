package org.openmrs.module.drools.calculation;

import org.openmrs.ConceptDatatype;
import org.openmrs.module.drools.utils.DroolsDateUtils;
import java.util.Date;

public class OperandsBuilder {

    private final Object originalLeft;
    private final Object originalRight;
    private final ConceptDatatype datatype;

    public OperandsBuilder(Object left, Object right, ConceptDatatype datatype) {
        this.originalLeft = left;
        this.originalRight = right;
        this.datatype = datatype;
    }

    /**
     * Prepares and returns refined operands, applying any necessary transformation
     * based on the datatype (e.g., extracting time if needed).
     */
    public Operands prepareOperands() {
        Object refinedLeft = originalLeft;
        Object refinedRight = CalculationUtils.refineRhsOperand(originalRight, datatype);
        if (datatype.isTime() && originalLeft instanceof Date) {
            refinedLeft = DroolsDateUtils.toLocalTime((Date) originalLeft);
        }

        return new Operands(refinedLeft, refinedRight);
    }

    public static final class Operands {
        private final Object left;
        private final Object right;

        public Operands(Object left, Object right) {
            this.left = left;
            this.right = right;
        }

        public Object getLeft() {
            return left;
        }

        public Object getRight() {
            return right;
        }
    }
}
