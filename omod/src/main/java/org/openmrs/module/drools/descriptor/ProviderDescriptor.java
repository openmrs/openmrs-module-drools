package org.openmrs.module.drools.descriptor;

import java.util.List;

public class ProviderDescriptor {

    private boolean isEnabled;
    private List<RuleDescriptor> rules;
    private List<SessionConfigDescriptor> sessionConfigs;
    public ProviderDescriptor() {
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public List<RuleDescriptor> getRules() {
        return rules;
    }

    public void setRules(List<RuleDescriptor> rules) {
        this.rules = rules;
    }

    public List<SessionConfigDescriptor> getSessionConfigs() {
        return sessionConfigs;
    }

    public void setSessionConfigs(List<SessionConfigDescriptor> sessionConfigs) {
        this.sessionConfigs = sessionConfigs;
    }

}
