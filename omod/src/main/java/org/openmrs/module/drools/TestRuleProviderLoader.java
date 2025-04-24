package org.openmrs.module.drools;

import java.util.List;
import java.util.Map;

import org.kie.api.io.ResourceType;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.RuleSessionConfig;

import org.openmrs.module.drools.session.ExternalEvaluator;

public class TestRuleProviderLoader implements RuleProviderLoader {

    @Override
    public List<RuleProvider> loadRuleProviders() {
        return List.of(new TestRuleProvider());
    }

    class TestRuleProvider implements RuleProvider {

        @Override
        public Boolean isEnabled() {
            return true;
        }

        @Override
        public List<RuleResource> getRuleResources() {
            return List.of(new RuleResource("Test Rules", "rules/age_rules.xlsx", ResourceType.DTABLE));
        }

        @Override
        public List<RuleSessionConfig> getSessionConfigs() {
            return List.of(
                    new RuleSessionConfig("Test session", true, true, null, null, List.of(new TestEventListener())));
        }

        @Override
        public Map<String, ExternalEvaluator> getExternalEvaluators() {
            return Map.of();
        }
    }
}
