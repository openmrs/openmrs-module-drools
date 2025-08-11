package org.openmrs.module.drools.session;

import org.kie.api.runtime.rule.Match;

public class AgendaGroupResolutionException extends RuntimeException {
    public AgendaGroupResolutionException(Match match) {
        super("Cannot resolve agenda group from match: " + match.getClass().getName()
                + ", rule=" + match.getRule().getName());
    }
}
