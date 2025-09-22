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
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        Context.getRegisteredComponents(RuleProviderLoader.class).forEach(ruleProviderLoader -> {
            try {
                ruleProviderLoader.loadRuleProviders().forEach(droolsEngineService::registerRuleProvider);
            } catch (Exception e) {
                log.error("Error loading rule providers", e);
            }
        });
        droolsEngineService.getSessionsForAutoStart().forEach(sessionConfig -> {
            KieSession session = droolsEngineService.requestSession(sessionConfig.getSessionId());
            session.fireAllRules();
            openSessions.add(session);
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
