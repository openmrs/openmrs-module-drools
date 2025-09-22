package org.openmrs.module.drools;

import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.drools.event.DroolsEventsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsActivator extends BaseModuleActivator implements DaemonTokenAware {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DroolsEngineRunner runner = new DroolsEngineRunner();

    /**
     * @see #started()
     */
    public void started() {
        runner.startDroolsEngine();
        log.info("Started OpenMRS Drools Engine");
    }

    /**
     * @see #shutdown()
     */
    public void shutdown() {
        // dispose of sessions
        runner.shutdown();
        log.info("OpenMRS Drools Engine stopped");
    }

    @Override
    public void setDaemonToken(DaemonToken token) {
        DroolsEventsManager.setDaemonToken(token);
        DroolsEngineRunner.setDaemonToken(token);
    }
}
