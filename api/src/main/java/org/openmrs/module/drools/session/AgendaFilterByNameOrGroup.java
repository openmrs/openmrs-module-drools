package org.openmrs.module.drools.session;

import org.apache.commons.lang3.StringUtils;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.drools.core.reteoo.RuleTerminalNodeLeftTuple;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AgendaFilterByNameOrGroup implements AgendaFilter {

    private final Set<String> ruleNames;
    private final String agendaGroup;

    public AgendaFilterByNameOrGroup(Collection<String> ruleNames, String agendaGroup) {
        this.ruleNames = (ruleNames != null && !ruleNames.isEmpty()) ? new HashSet<>(ruleNames) : null;
        this.agendaGroup = agendaGroup;
    }

    @Override
    public boolean accept(Match match) {
        if (StringUtils.isNotBlank(agendaGroup) && isNullOrEmpty(ruleNames)) {
            return handleGroup(match, agendaGroup);
        } else if (!isNullOrEmpty(ruleNames)) {
            return ruleNames.contains(match.getRule().getName());
        }
        return true;
    }

    private boolean handleGroup(Match match, String allowedGroup) {
        String candidate;
        if (match instanceof RuleTerminalNodeLeftTuple) {
            candidate = ((RuleTerminalNodeLeftTuple) match).getAgendaGroup().getName();
        } else if (match.getRule() instanceof RuleImpl) {
            candidate = ((RuleImpl)match.getRule()).getAgendaGroup();
        } else {
            throw new AgendaGroupResolutionException(match);
        }
        return candidate.equals(allowedGroup);
    }

    private boolean isNullOrEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }
}
