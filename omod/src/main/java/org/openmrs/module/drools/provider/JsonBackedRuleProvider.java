package org.openmrs.module.drools.provider;

import org.kie.api.io.ResourceType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.drools.descriptor.ProviderDescriptor;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.param.DroolsParameterType;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.ExternalEvaluator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonBackedRuleProvider implements RuleProvider {
    private final ProviderDescriptor config;
    private final Path parentDirectory;

    public JsonBackedRuleProvider(ProviderDescriptor config, Path parentDirectory) {
        this.config = config;
        this.parentDirectory = parentDirectory;
    }
    @Override
    public Boolean isEnabled() {
        return config.getIsEnabled();
    }

    @Override
    public List<RuleResource> getRuleResources() {
        return config.getRules().stream().map(r -> {
            Path resourcePath = parentDirectory.resolve(r.getPath()).normalize();
            return new RuleResource(r.getName(), resourcePath.toString(), inferResourceType(resourcePath.toString()));
        }).collect(Collectors.toList());
    }

    @Override
    public List<DroolsSessionConfig> getSessionConfigs() {
        DroolsCalculationService calculationService = Context.getRegisteredComponents(DroolsCalculationService.class)
                .get(0);
        return config.getSessionConfigs().stream()
                .map(sc -> {
                    DroolsSessionConfig cfg = new DroolsSessionConfig();
                    cfg.setSessionId(sc.getSessionId());
                    cfg.setReturnObjectsTypeClassName(sc.getReturnObjectsTypeClassName());
                    cfg.setAgendaGroup(sc.getAgendaGroup());
                    cfg.getGlobals().put("service", calculationService);

                    if (sc.getParams() != null) {
                        Set<DroolsParameterDefinition> parameterDefinitions = sc.getParams().stream().map(p -> {
                            DroolsParameterType type = DroolsParameterType.valueOf(p.getType());
                            return new DroolsParameterDefinition(
                                    p.getName(), type, p.isRequired());
                        }).collect(Collectors.toSet());
                        cfg.setParameterDefinitions(parameterDefinitions);
                    }
                    return cfg;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, ExternalEvaluator> getExternalEvaluators() {
        return null;
    }

    private ResourceType inferResourceType(String path) {
        if (path.endsWith(".drl")) return ResourceType.DRL;
        if (path.endsWith(".xls") || path.endsWith(".xlsx")) return ResourceType.DTABLE;
        throw new IllegalArgumentException("Unsupported resource type: " + path);
    }
}
