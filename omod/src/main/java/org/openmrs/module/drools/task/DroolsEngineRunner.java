package org.openmrs.module.drools.task;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.TestRuleProviderLoader;
import org.openmrs.module.drools.api.DroolsEngineService;

public class DroolsEngineRunner implements Runnable {

    private static DaemonToken daemonToken;

    public DroolsEngineRunner() {
    }

    @Override
    public void run() {
        // TODO: We need to introduce session provider loaders eg. classpath loader,
        // implementation-based loader, etc.
        // to load rules and then start Drools engine on module startup

        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        new TestRuleProviderLoader().loadRuleProviders().forEach(ruleProvider -> {
            droolsEngineService.registerRuleProvider(ruleProvider);
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
