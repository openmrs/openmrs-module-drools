package org.openmrs.module.drools.patientflags;

import org.openmrs.api.context.Context;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.api.FlagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientFlagInitializer {
    private static Logger log = LoggerFactory.getLogger(PatientFlagInitializer.class);

    public static void initialize() {
        createFlagIfNotExists("Sepsis", "{\n" +
                "  \"session\": \"Sepsis\",\n" +
                "  \"rules\": [],\n" +
                "  \"agendaGroup\": \"sepsis\"\n" +
                "}");
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
