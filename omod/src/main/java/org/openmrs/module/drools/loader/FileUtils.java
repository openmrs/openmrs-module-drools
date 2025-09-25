package org.openmrs.module.drools.loader;

import org.openmrs.api.context.Context;
import org.openmrs.module.drools.DroolsModuleConstants;
import org.openmrs.util.OpenmrsUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static Path getDroolsInstallationDir() {
        Path omrsAppDir = Paths.get(OpenmrsUtil.getApplicationDataDirectory());
        String droolsConfigDir = Context.getAdministrationService().getGlobalProperty(DroolsModuleConstants.GP_LOCAL_STORAGE_DIR);
        return omrsAppDir.resolve(droolsConfigDir);
    }
}
