package org.openmrs.module.drools;

import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleFactory;
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
        debug();
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

    public void debug() {

        ClassLoader droolsClassLoader = ModuleFactory.getModuleClassLoader("drools");
        if (droolsClassLoader != null) {
            try {
                // Pre-load schema type system from Drools context
                droolsClassLoader.loadClass("org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument");
            } catch (ClassNotFoundException e) {
                log.warn("Could not pre-load schema classes", e);
            }
        }

        System.out.println("--------------------------------------------------");

        Class<?> c = org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument.class;
        System.out.println("ThemeDocument loader: " + c.getClassLoader());
        System.out.println("ThemeDocument location: " + c.getProtectionDomain().getCodeSource().getLocation());

        Class<?> xmlbeansIface = org.apache.xmlbeans.SchemaTypeLoader.class;
        System.out.println("SchemaTypeLoader loader: " + xmlbeansIface.getClassLoader());
        System.out.println("SchemaTypeLoader location: " + xmlbeansIface.getProtectionDomain().getCodeSource().getLocation());

        Class<?> xmlbeansImpl = org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl.class;
        System.out.println("SchemaTypeSystemImpl loader: " + xmlbeansImpl.getClassLoader());
        System.out.println("SchemaTypeSystemImpl location: " + xmlbeansImpl.getProtectionDomain().getCodeSource().getLocation());

        System.out.println("--------------------------------------------------");

    }
}
