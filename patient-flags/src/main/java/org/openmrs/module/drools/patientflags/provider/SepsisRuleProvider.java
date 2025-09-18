package org.openmrs.module.drools.patientflags.provider;

import org.kie.api.io.ResourceType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.param.DroolsParameterType;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SepsisRuleProvider implements RuleProvider {

    private static final String RULES_PATH = "org/openmrs/module/drools/patientflags/sepsis_rules_table.drl.xlsx";

    @Override
    public Boolean isEnabled() {
        return false;
    }

    @Override
    public List<RuleResource> getRuleResources() {
        return List.of(new RuleResource("Sepsis Rules", RULES_PATH, ResourceType.DTABLE));
    }

    @Override
    public List<DroolsSessionConfig> getSessionConfigs() {
        DroolsSessionConfig config = new DroolsSessionConfig();
        config.setSessionId("Sepsis");
        config.setAutoStart(true);

        config.getGlobals().put("calculationService", Context.getRegisteredComponents(DroolsCalculationService.class)
                .get(0));
        config.getGlobals().put("flaggedPatients", new ArrayList<>());
        config.getParameterDefinitions().add(new DroolsParameterDefinition("patient", DroolsParameterType.PATIENT_UUID, true));
        config.setReturnObjectsTypeClassName("org.openmrs.module.drools.patientflags.FlaggedPatient");
        config.setAgendaGroup("sepsis");

        return List.of(config);
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }
}
