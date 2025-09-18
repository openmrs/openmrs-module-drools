package org.openmrs.module.drools.patientflags;

import org.kie.api.io.ResourceType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TestSepsisRuleProvider implements RuleProvider  {
    private static final String RULES_PATH = "org/openmrs/module/drools/patientflags/test_sepsis_rules_table.drl.xlsx";

    @Override
    public Boolean isEnabled() {
        return true;
    }

    @Override
    public List<RuleResource> getRuleResources() {
        return List.of(new RuleResource("Test Sepsis Rules", RULES_PATH, ResourceType.DTABLE));
    }

    @Override
    public List<DroolsSessionConfig> getSessionConfigs() {
        DroolsSessionConfig config = new DroolsSessionConfig();
        config.setSessionId("Test Sepsis");
        config.setAutoStart(false);
        config.getGlobals().put("service", Context.getRegisteredComponents(DroolsCalculationService.class).get(0));
        return List.of(config);
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }
}
