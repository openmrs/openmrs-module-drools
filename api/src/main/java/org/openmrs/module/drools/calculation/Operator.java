package org.openmrs.module.drools.calculation;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.ConceptDatatype;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public enum Operator {

    /**
     * Equals
     */
    EQUALS(new HashSet<>(Arrays.asList(ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME, ConceptDatatype.BOOLEAN,
            ConceptDatatype.CODED, ConceptDatatype.NUMERIC))) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype())
                    .prepareOperands();
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
    LT(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME))) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype())
                    .prepareOperands();
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
    GT(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME))) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype())
                    .prepareOperands();
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
    LTE(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME))) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype())
                    .prepareOperands();
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
    GTE(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME))) {
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            OperandsBuilder.Operands operands = new OperandsBuilder(left, right, datatype.getDatatype())
                    .prepareOperands();
            Comparator<Object> comparator = DatatypeComparatorRegistry.getComparator(datatype.getDatatypeCode());
            if (comparator != null) {
                int result = comparator.compare(operands.getLeft(), operands.getRight());
                return result > 0 || result == 0;
            } else {
                throw new IllegalStateException("Failed to resolve Comparator");
            }
        }
    },

    /**
     * Checks if a given obs value exists -- Applicable to all obs
     */
    EXISTS(new HashSet<>(Arrays.asList(ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME, ConceptDatatype.BOOLEAN,
            ConceptDatatype.CODED, ConceptDatatype.NUMERIC))) {
        @Autowired
        public boolean apply(Object left, Object ignored, ConceptDatatypeWrapper datatype) {
            if (left == null) {
                return false;
            }
            if (left instanceof String) {
                return StringUtils.isNotBlank((String) left);
            }
            return true;
        }
    },

    /**
     * Applicable to numerics and dates
     */
    BETWEEN(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.DATE, ConceptDatatype.DATETIME, ConceptDatatype.TIME))) {
        // TODO: Add implementation
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            throw new UnsupportedOperationException("Unimplemented operator \"BETWEEN\"");
        }
    },

    /**
     * Applicable to numerics, coded. Example usage:
     * {@code checkLatestObs(patient, "CIEL:123", Operator.IN,  value1, value2, value3)})
     */
    IN(new HashSet<>(Arrays.asList(ConceptDatatype.NUMERIC, ConceptDatatype.CODED))) {
        // TODO: Add implementation
        @Autowired
        public boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype) {
            throw new UnsupportedOperationException("Unimplemented operator \"IN\"");
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
     * @param left     the left-hand operand (e.g., obs value)
     * @param right    the right-hand operand (e.g., comparison value)
     * @param datatype the concept datatype
     * @return true if the comparison holds; false otherwise
     */
    public abstract boolean apply(Object left, Object right, ConceptDatatypeWrapper datatype);

}
