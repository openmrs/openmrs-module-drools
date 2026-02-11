package org.openmrs.module.drools;

import org.kie.api.runtime.KieSession;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.loader.RuleProviderLoader;
import org.openmrs.module.drools.api.DroolsEngineService;
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
        log.info("Starting Drools Engine auto-start process");
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        log.debug("Loading rule providers");
        Context.getRegisteredComponents(RuleProviderLoader.class).forEach(ruleProviderLoader -> {
            try {
                log.debug("Processing rule provider loader: {}", ruleProviderLoader.getClass().getSimpleName());
                ruleProviderLoader.loadRuleProviders().forEach(ruleProvider -> {
                    log.debug("Registering rule provider: {}", ruleProvider.getClass().getSimpleName());
                    droolsEngineService.registerRuleProvider(ruleProvider);
                });
            } catch (Exception e) {
                log.error("Error loading rule providers from loader: {}", ruleProviderLoader.getClass().getSimpleName(), e);
            }
        });

        List<org.openmrs.module.drools.session.DroolsSessionConfig> autoStartSessions = droolsEngineService.getSessionsForAutoStart();
        log.info("Found {} session(s) configured for auto-start", autoStartSessions.size());

        autoStartSessions.forEach(sessionConfig -> {
            try {
                String sessionId = sessionConfig.getSessionId();
                log.info("Auto-starting session: {}", sessionId);
                long startTime = System.currentTimeMillis();

                KieSession session = droolsEngineService.requestSession(sessionId);
                int rulesFired = session.fireAllRules();
                openSessions.add(session);

                long duration = System.currentTimeMillis() - startTime;
                log.info("Auto-start session {} completed: {} rules fired in {}ms", sessionId, rulesFired, duration);
            } catch (Exception e) {
                log.error("Error auto-starting session: {}", sessionConfig.getSessionId(), e);
            }
        });

        log.info("Drools Engine auto-start process completed. {} session(s) active", openSessions.size());
    }

    public void startDroolsEngine() {
        Daemon.runInDaemonThreadWithoutResult(this, daemonToken);
    }

    public void shutdown() {
        log.info("Shutting down Drools Engine, disposing {} open session(s)", openSessions.size());
        this.openSessions.forEach(session -> {
            log.debug("Disposing session");
            session.dispose();
        });
        log.info("Drools Engine shutdown complete");
    }

    public static void setDaemonToken(DaemonToken token) {
        daemonToken = token;
    }
}
