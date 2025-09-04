package org.openmrs.module.drools.patientflags.provider;

import org.kie.api.io.ResourceType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.param.DroolsParameterType;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FormsRuleProvider implements RuleProvider {

    private static final String RULES_PATH = "org/openmrs/module/drools/forms/mch_form_rules_table.drl.xlsx";

    @Override
    public Boolean isEnabled() {
        return true;
    }

    @Override
    public List<RuleResource> getRuleResources() {
        return List.of(new RuleResource("MCH Form Rules", RULES_PATH, ResourceType.DTABLE));
    }

    @Override
    public List<DroolsSessionConfig> getSessionConfigs() {
        DroolsSessionConfig config = new DroolsSessionConfig();
        config.setSessionId("Forms");
        config.getGlobals().put("service", Context.getRegisteredComponents(DroolsCalculationService.class)
                .get(0));
        config.getParameterDefinitions().add(new DroolsParameterDefinition("patient", DroolsParameterType.PATIENT_UUID, true));
        config.setReturnObjectsTypeClassName("org.openmrs.Form");
        config.setAgendaGroup("forms");
        return List.of(config);
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }
}
