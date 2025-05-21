package org.openmrs.module.drools.patientflags;

import java.util.List;
import java.util.Map;

import org.kie.api.io.ResourceType;
import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.springframework.stereotype.Component;

@Component
public class PatientFlagsRuleProvider implements RuleProvider {

    private static final String RULES_PATH = "org/openmrs/module/drools/bp_rules.drl.xlsx";

    @Override
    public List<RuleResource> getRuleResources() {
        return List.of(new RuleResource("Blood Pressure Rules", RULES_PATH, ResourceType.DTABLE));
    }

    @Override
    public List<RuleSessionConfig> getSessionConfigs() {
        RuleSessionConfig config = new RuleSessionConfig();
        config.setSessionId("Blood Pressure");
        config.setStateful(true);
        config.setAutoStart(false);
        Map<String, Object> globals = config.getGlobals();
        globals.put("SYSTOLIC_UUID", "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        globals.put("DIASTOLIC_UUID", "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        return List.of(config);
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return true;
    }
}
