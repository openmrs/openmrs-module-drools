package org.openmrs.module.drools.patientflags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.api.FlagService;

public class PatientFlagInitializer {

    private static Log log = LogFactory.getLog(PatientFlagInitializer.class);

    public static void initialize() {
        createFlagIfNotExists("High BP", "Blood Pressure");
        createFlagIfNotExists("Critical High BP", "Blood Pressure");
        createFlagIfNotExists("Low BP", "Blood Pressure");
    }

    private static void createFlagIfNotExists(String flagName, String criteria) {
        FlagService flagService = Context.getService(FlagService.class);
        Flag existingFlag = flagService.getFlagByName(flagName);
        if (existingFlag == null) {
            Flag flag = new Flag(flagName, criteria, flagName);
            flag.setEvaluator("org.openmrs.module.drools.patientflags.DroolsFlagEvaluator");
            log.info("Creating flag: " + flagName);
            flagService.saveFlag(flag);
        } else {
            log.info("Flag already exists: " + flagName);
        }
    }
}
