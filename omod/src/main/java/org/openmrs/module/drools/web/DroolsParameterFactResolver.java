package org.openmrs.module.drools.web;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.param.DroolsParameterType;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.openmrs.module.drools.param.DroolsParameterType.*;

@Component
public class DroolsParameterFactResolver {

    private Map<DroolsParameterType, Function<String, OpenmrsObject>> resolvers;

    public DroolsParameterFactResolver() {

    }

    public List<Object> resolveFacts(DroolsSessionConfig config, Map<String, String> params) {
        if (resolvers == null) {
            resolvers = initializeResolvers();
        }
        List<Object> facts = new ArrayList<>();
        for (DroolsParameterDefinition def : config.getParameterDefinitions()) {
            String value = params.get(def.getParameterName());
            if (StringUtils.isBlank(value)) {
                continue;
            }
            // TODO: do we need this?
            if (def.getParameterType() == DroolsParameterType.LITERAL) {
                facts.add(value);
            }
            Function<String, OpenmrsObject> resolver = resolvers.get(def.getParameterType());

            if (resolver != null) {
                facts.add(resolver.apply(value));
            } else {
                throw new IllegalArgumentException("Unsupported parameter: " + def.getParameterName());
            }
        }
        return facts;
    }

    private Map<DroolsParameterType, Function<String, OpenmrsObject>> initializeResolvers() {
        // TODO: should we use a more dynamic approach that is reflection based?
        resolvers = new HashMap<>();
        resolvers.put(PATIENT_UUID, uuid -> Context.getPatientService().getPatientByUuid(uuid));
        resolvers.put(ENCOUNTER_UUID, uuid -> Context.getEncounterService().getEncounterByUuid(uuid));
        resolvers.put(OBS_UUID, uuid -> Context.getObsService().getObsByUuid(uuid));
        resolvers.put(VISIT_UUID, uuid -> Context.getVisitService().getVisitByUuid(uuid));
        resolvers.put(FORM_UUID, uuid -> Context.getFormService().getFieldByUuid(uuid));
        return resolvers;
    }

}
