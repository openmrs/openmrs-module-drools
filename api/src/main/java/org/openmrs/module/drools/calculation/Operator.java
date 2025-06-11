package org.openmrs.module.drools.calculation;

import org.openmrs.ConceptDatatype;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.Set;

public enum Operator {

    /**
     * Equals
     */
    EQUALS(Set.of(ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME, ConceptDatatype.BOOLEAN, ConceptDatatype.CODED, ConceptDatatype.NUMERIC)) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype()).prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                return comparator.compare(operands.getLeft(), operands.getRight()) == 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    },
    /**
     * Less than
     */
    LT(Set.of(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME)) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype()).prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                return comparator.compare(operands.getLeft(), operands.getRight()) < 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    },
    /**
     * Greater than
     */
    GT(Set.of(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME)) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype()).prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                return comparator.compare(operands.getLeft(), operands.getRight()) > 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    },
    /**
     * Less than or equal to
     */
    LTE(Set.of(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME)) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype()).prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                 int result = comparator.compare(operands.getLeft(), operands.getRight());
                 return result < 0 || result == 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    },
    /**
     * Greater than or equal to
     */
    GTE(Set.of(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME)) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype()).prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                int result = comparator.compare(operands.getLeft(), operands.getRight());
                return result > 0 || result == 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    };

    private final Set<String> supportedDatatypes;

    Operator(Set<String> supportedDatatypes) {
        this.supportedDatatypes = supportedDatatypes;
    }

    public Set<String> getSupportedDatatypes() {
        return supportedDatatypes;
    }

    /**
     * Applies this operator to the given operands.
     *
     * @param left  the left-hand operand (e.g., obs value)
     * @param right the right-hand operand (e.g., comparison value)
     * @param datatype the concept datatype
     * @return true if the comparison holds; false otherwise
     */
    public abstract boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype);

}
