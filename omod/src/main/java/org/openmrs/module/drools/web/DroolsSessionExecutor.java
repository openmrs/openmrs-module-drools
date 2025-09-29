package org.openmrs.module.drools.web;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DroolsSessionExecutor {

    @Autowired
    private DroolsEngineService droolsService;

    @Autowired
    private DroolsParameterFactResolver parameterFactResolver;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(5);
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * Synchronous execution
     */
    public DroolsExecutionResult executeSession(String sessionId, Map<String, String> params) {
        return performExecution(sessionId, params);
    }

    /**
     * Asynchronous execution
     */
    public CompletableFuture<DroolsExecutionResult> executeSessionAsync(String sessionId, Map<String, String> params) {
        return CompletableFuture.supplyAsync(() -> performExecution(sessionId, params), executorService);
    }

    private DroolsExecutionResult performExecution(String sessionId, Map<String, String> params) {
        DroolsSessionConfig config = droolsService.getSessionConfig(sessionId);
        String clazzName = config.getReturnObjectsTypeClassName();
        if (StringUtils.isBlank(clazzName)) {
            throw new IllegalArgumentException(
                    "Missing return object type class configuration for session '" + sessionId + "'. " +
                            "The session configuration must specify a valid fully-qualified class name " +
                            "in the 'returnObjectsTypeClassName' property."
            );
        }

        List<Object> facts = parameterFactResolver.resolveFacts(config, params);

        return droolsService.evaluate(sessionId, facts, clazzName);
    }
}
