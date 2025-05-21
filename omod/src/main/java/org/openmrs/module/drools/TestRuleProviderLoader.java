package org.openmrs.module.drools;

import org.kie.api.io.ResourceType;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.openmrs.module.drools.session.ExternalEvaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRuleProviderLoader implements RuleProviderLoader {

    @Override
    public List<RuleProvider> loadRuleProviders() {
        return Collections.singletonList(new TestRuleProvider());
    }

    class TestRuleProvider implements RuleProvider {

        @Override
        public Boolean isEnabled() {
            return true;
        }

        @Override
        public List<RuleResource> getRuleResources() {
            return Collections.singletonList(new RuleResource("Test Rules", "org/openmrs/module/drools/age_rules.drl.xlsx", ResourceType.DTABLE));
        }

        @Override
        public List<RuleSessionConfig> getSessionConfigs() {
            return Collections.singletonList(
                    new RuleSessionConfig("Test session", true, true, null, null, Collections.singletonList(new TestEventListener())));
        }

        @Override
        public Map<String, ExternalEvaluator> getExternalEvaluators() {
            return new HashMap<>();
        }
    }
}
