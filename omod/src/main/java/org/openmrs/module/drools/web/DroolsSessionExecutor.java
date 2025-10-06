package org.openmrs.module.drools.web;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.SessionMetadata;
import org.openmrs.module.drools.session.ThreadSafeSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DroolsSessionExecutor {

    @Autowired
    private DroolsEngineService droolsService;

    @Autowired
    private DroolsParameterFactResolver parameterFactResolver;

    @Autowired
    private ThreadSafeSessionRegistry sessionRegistry;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public DroolsExecutionResult executeSession(String sessionId, Map<String, String> params) {
        return performExecution(sessionId, params);
    }

    public CompletableFuture<DroolsExecutionResult> executeSessionAsync(String sessionId, Map<String, String> params) {
        return CompletableFuture.supplyAsync(() -> performExecution(sessionId, params), executorService);
    }

    /**
     * Executes rules against an existing session in the registry.
     * This method retrieves a session from the registry and executes rules on it
     * without creating a new session or disposing it afterward.
     *
     * @param sessionId the ID of the existing session
     * @param params execution parameters
     * @return execution result
     * @throws IllegalArgumentException if session doesn't exist in registry
     */
    public DroolsExecutionResult executeAgainstExistingSession(String sessionId, Map<String, String> params) {
        Optional<KieSession> sessionOpt = sessionRegistry.getSession(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException(
                    "Session '" + sessionId + "' not found in registry. " +
                            "Only auto-startable sessions can be queried via REST."
            );
        }

        return performExecution(sessionId, params);
    }

    /**
     * Executes rules with session reuse: checks registry first, falls back to new session.
     * If session exists in registry, reuses it; otherwise creates new session per normal flow.
     *
     * @param sessionId the session ID
     * @param params execution parameters
     * @return execution result
     */
    public DroolsExecutionResult executeSessionWithReuse(String sessionId, Map<String, String> params) {
        if (sessionRegistry.sessionExists(sessionId)) {
            return executeAgainstExistingSession(sessionId, params);
        }
        return performExecution(sessionId, params);
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
