package org.openmrs.module.drools;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.loader.RuleProviderLoader;
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
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        Context.getRegisteredComponents(RuleProviderLoader.class).forEach(ruleProviderLoader -> {
            try {
                ruleProviderLoader.loadRuleProviders().forEach(droolsEngineService::registerRuleProvider);
            } catch (Exception e) {
                log.error("Error loading rule providers", e);
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
