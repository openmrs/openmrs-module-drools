package org.openmrs.module.drools.task;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.RuleProviderLoader;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsEngineRunner implements Runnable {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static DaemonToken daemonToken;

    public DroolsEngineRunner() {
    }

    @Override
    public void run() {
        // TODO: We need to introduce session provider loaders eg. classpath loader,
        // implementation-based loader, etc.
        // to load rules and then start Drools engine on module startup

        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        Context.getRegisteredComponents(RuleProviderLoader.class).forEach(ruleProviderLoader -> {
            try {
                ruleProviderLoader.loadRuleProviders().forEach(droolsEngineService::registerRuleProvider);
            } catch (Exception e) {
                log.error("Error loading rule provider(s)", e);
            }
        });
        droolsEngineService.getSessionsForAutoStart().forEach(sessionConfig -> {
            droolsEngineService.requestSession(sessionConfig.getSessionId()).fireAllRules();
        });
    }

    public void startDroolsEngine() {
        Daemon.runInDaemonThread(this, daemonToken);
    }

    public static void setDaemonToken(DaemonToken token) {
        daemonToken = token;
    }
}
