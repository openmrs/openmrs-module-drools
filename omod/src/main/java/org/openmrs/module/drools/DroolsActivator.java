package org.openmrs.module.drools;

import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.drools.event.DroolsEventsManager;
import org.openmrs.module.drools.patientflags.PatientFlagInitializer;
import org.openmrs.module.drools.task.DroolsEngineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsActivator extends BaseModuleActivator implements DaemonTokenAware {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @see #started()
     */
    public void started() {
        PatientFlagInitializer.initialize();
        new DroolsEngineRunner().startDroolsEngine();
        log.info("Started OpenMRS Drools Engine");
    }

    /**
     * @see #shutdown()
     */
    public void shutdown() {
        // TODO: Add shutdown logic here
        // eventsManager.unSubscribeAll();
        // dispose all sessions
        log.info("OpenMRS Drools Engine stopped");
    }

    @Override
    public void setDaemonToken(DaemonToken token) {
        DroolsEventsManager.setDaemonToken(token);
        DroolsEngineRunner.setDaemonToken(token);
    }
}
