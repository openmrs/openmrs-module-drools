package org.openmrs.module.drools.patientflags;

import java.util.List;
import java.util.Map;

import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.RuleSessionConfig;

public class PatientFlagsRuleProvider implements RuleProvider {

    private static final String RULES_PATH = "rules/patient-flags.drl";

    @Override
    public List<RuleResource> getRuleResources() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRuleResources'");
    }

    @Override
    public List<RuleSessionConfig> getSessionConfigs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSessionConfigs'");
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExternalEvaluators'");
    }
}
