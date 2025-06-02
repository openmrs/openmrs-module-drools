package org.openmrs.module.drools.patientflags.provider;

import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.RuleSessionConfig;

import java.util.List;
import java.util.Map;

public class SepsisRuleProvider implements RuleProvider  {
    @Override
    public Boolean isEnabled() {
        return true;
    }

    @Override
    public List<RuleResource> getRuleResources() {
        return null;
    }

    @Override
    public List<RuleSessionConfig> getSessionConfigs() {
        return null;
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }
}
