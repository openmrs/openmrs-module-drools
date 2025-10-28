package org.openmrs.module.drools;

import org.kie.api.runtime.KieSession;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.loader.RuleProviderLoader;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DroolsEngineRunner implements Runnable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static DaemonToken daemonToken;

    private final List<KieSession> openSessions = new ArrayList<>();

    public DroolsEngineRunner() {
    }

    @Override
    public void run() {
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);
        SessionRegistry sessionRegistry = Context.getService(SessionRegistry.class);

        // Load rule providers
        Context.getRegisteredComponents(RuleProviderLoader.class).forEach(ruleProviderLoader -> {
            try {
                ruleProviderLoader.loadRuleProviders().forEach(droolsEngineService::registerRuleProvider);
            } catch (Exception e) {
                log.error("Error loading rule providers", e);
            }
        });
        
        // Create and register auto-startable sessions
        droolsEngineService.getSessionsForAutoStart().forEach(sessionConfig -> {
            String sessionId = sessionConfig.getSessionId();
            KieSession session = droolsEngineService.requestSession(sessionId);
            
            // Fire initial rules
            session.fireAllRules();
            
            // Register the session in the registry
            boolean registered = sessionRegistry.registerSession(sessionId, session);
            if (registered) {
                openSessions.add(session);
                log.info("Auto-started and registered session '{}'", sessionId);
            } else {
                log.warn("Failed to register auto-startable session '{}', disposing", sessionId);
                session.dispose();
            }
        });
    }

    public void startDroolsEngine() {
        Daemon.runInDaemonThreadWithoutResult(this, daemonToken);
    }

    public void shutdown() {
        this.openSessions.forEach(KieSession::dispose);
    }

    public static void setDaemonToken(DaemonToken token) {
        daemonToken = token;
    }
}
