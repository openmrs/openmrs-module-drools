package org.openmrs.module.drools.web;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.ThreadSafeSessionRegistry;
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
     * Executes rules against an existing session in the registry using SessionLease.
     * This method checks out the session with a lock, executes rules, and automatically
     * releases the lock via try-with-resources.
     * 
     * <p>This prevents Thread1/Thread2 data race conditions through the check-out mechanism.</p>
     *
     * @param sessionId the ID of the existing session
     * @param params execution parameters
     * @return execution result
     * @throws IllegalArgumentException if session doesn't exist in registry
     */
    public DroolsExecutionResult executeAgainstExistingSession(String sessionId, Map<String, String> params) {
        // Verify session exists first
        if (!sessionRegistry.sessionExists(sessionId)) {
            throw new IllegalArgumentException(
                    "Session '" + sessionId + "' not found in registry. " +
                            "Only auto-startable sessions can be queried via REST."
            );
        }

        try {
            // Check out session with 10-second timeout using try-with-resources
            try (org.openmrs.module.drools.session.SessionLease lease = 
                    sessionRegistry.checkOutSession(sessionId, 10, java.util.concurrent.TimeUnit.SECONDS)) {
                
                // Get session config and prepare execution
                DroolsSessionConfig config = droolsService.getSessionConfig(sessionId);
                String clazzName = config.getReturnObjectsTypeClassName();
                if (StringUtils.isBlank(clazzName)) {
                    throw new IllegalArgumentException(
                            "Missing return object type class configuration for session '" + sessionId + "'. " +
                                    "The session configuration must specify a valid fully-qualified class name " +
                                    "in the 'returnObjectsTypeClassName' property."
                    );
                }

                // Resolve facts from parameters
                List<Object> facts = parameterFactResolver.resolveFacts(config, params);

                // Get the checked-out session
                KieSession session = lease.getSession();

                // Insert facts and fire rules
                facts.forEach(session::insert);
                int fired = session.fireAllRules();

                // Get results
                List<?> results = droolsService.getSessionObjects(session, 
                        resolveClass(clazzName, session.getKieBase()));

                @SuppressWarnings("unchecked")
                List<Object> objectResults = (List<Object>) results;
                return new org.openmrs.module.drools.session.DroolsExecutionResult(
                        sessionId, fired, objectResults);
                
            } // Lock automatically released here
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for session lock", e);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new RuntimeException("Timeout waiting for session lock for '" + sessionId + "'", e);
        }
    }
    
    /**
     * Helper method to resolve class by name.
     */
    private Class<?> resolveClass(String className, org.kie.api.KieBase kieBase) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Try Drools-declared types
            org.kie.api.definition.type.FactType factType = kieBase.getFactType(
                    className.substring(0, className.lastIndexOf('.')),
                    className.substring(className.lastIndexOf('.') + 1)
            );
            if (factType != null) {
                return factType.getFactClass();
            }
            throw new RuntimeException("Could not resolve class: " + className, e);
        }
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
