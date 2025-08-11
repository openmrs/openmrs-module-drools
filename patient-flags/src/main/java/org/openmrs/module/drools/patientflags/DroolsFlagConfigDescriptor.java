package org.openmrs.module.drools.patientflags;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class DroolsFlagConfigDescriptor {

    @JsonProperty
    private String session;

    @JsonProperty
    private List<String> rules;

    @JsonProperty
    private String agendaGroup;

    public DroolsFlagConfigDescriptor() {
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public String getAgendaGroup() {
        return agendaGroup;
    }

    public void setAgendaGroup(String agendaGroup) {
        this.agendaGroup = agendaGroup;
    }
}
