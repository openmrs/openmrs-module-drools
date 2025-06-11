package org.openmrs.module.drools.calculation;

import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.module.drools.utils.DroolsDateUtils;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatatypeComparatorRegistry {
    private static final Map<String, Comparator<Object>> COMPARATORS = new HashMap<>();

    static {
        // NUMERIC
        COMPARATORS.put(ConceptDatatype.NUMERIC, Comparator.nullsFirst(Comparator.comparingDouble(obj -> ((Number) obj).doubleValue())));

        // DATE / DATETIME
        COMPARATORS.put(ConceptDatatype.DATE, Comparator.nullsFirst(Comparator.comparing(obj -> (Date) obj)));
        COMPARATORS.put(ConceptDatatype.DATETIME, Comparator.nullsFirst(Comparator.comparing(obj -> (Date) obj)));

        // TIME
        COMPARATORS.put(ConceptDatatype.TIME, Comparator.nullsFirst(Comparator.comparing(obj -> {
            if (obj instanceof Date) {
                return DroolsDateUtils.toLocalTime((Date) obj);
            }
            return (LocalTime) obj;
        })));

        // BOOLEAN
        COMPARATORS.put(ConceptDatatype.BOOLEAN, Comparator.nullsFirst(Comparator.comparing(obj -> (Boolean) obj)));

        // CODED (compare using .equals)
        COMPARATORS.put(ConceptDatatype.CODED, (o1, o2) -> ((Concept) o1).equals((Concept) o2) ? 0 : -1);

        // TEXT
        COMPARATORS.put(ConceptDatatype.TEXT, Comparator.comparing(Object::toString));
    }

    public static Comparator<Object> getComparator(String datatypeCode) {
        return COMPARATORS.get(datatypeCode);
    }
}
