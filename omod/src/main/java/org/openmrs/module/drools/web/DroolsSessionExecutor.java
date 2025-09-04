package org.openmrs.module.drools.web;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DroolsSessionExecutor {

    @Autowired
    private DroolsEngineService droolsService;

    @Autowired
    private DroolsParameterFactResolver parameterFactResolver;

    public DroolsExecutionResult executeSession(String sessionId, Map<String, String> params) {
        DroolsSessionConfig config = droolsService.getSessionConfig(sessionId);
        String clazzName = config.getReturnObjectsTypeClassName();
        Class<?> resultClazz;
        if (StringUtils.isBlank(clazzName)) {
            throw new IllegalArgumentException(
                    "Missing return object type class configuration for session '" + sessionId + "'. " +
                            "The session configuration must specify a valid fully-qualified class name " +
                            "in the 'returnObjectsTypeClassName' property."
            );
        }
        try {
            resultClazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            throw new APIException("Invalid return object type class configured for session '"
                    + sessionId + "': " + clazzName + ". Class not found on classpath.", e);
        }

        List<Object> facts = parameterFactResolver.resolveFacts(config, params);
        return droolsService.evaluate(sessionId, facts, resultClazz);
    }
}
