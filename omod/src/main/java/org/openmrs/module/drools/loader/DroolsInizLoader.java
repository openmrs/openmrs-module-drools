package org.openmrs.module.drools.loader;

import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.ConfigDirUtil;
import org.openmrs.module.initializer.api.loaders.BaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.openmrs.module.drools.loader.FileUtils.getDroolsInstallationDir;

@Component
public class DroolsInizLoader extends BaseLoader {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected Domain getDomain() {
        return null;
    }

    @Override
    public String getDomainName() {
        // TODO: use constant
        return "drools";
    }

    @Override
    public Integer getOrder() {
        return Domain.values().length + 1;
    }

    @Override
    public ConfigDirUtil getDirUtil() {
        return new ConfigDirUtil(iniz.getConfigDirPath(), iniz.getChecksumsDirPath(), getDomainName(), true);
    }

    @Override
    public void loadUnsafe(List<String> wildcardExclusions, boolean doThrow) throws Exception {
        try {
            ConfigDirUtil dirUtil = getDirUtil();

            loadDroolsConfig(dirUtil);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            if (doThrow) {
                log.error("Error loading Drools config", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void loadDroolsConfig(ConfigDirUtil dirUtil) throws IOException {
        if (!dirUtil.getFiles("json").isEmpty()) {
            Path srcDir = Paths.get(dirUtil.getDomainDirPath());
            Path targetDir = getDroolsInstallationDir();

            // clean up
            deleteDirectory(targetDir.toFile());
            // copy contents
            copyDirectory(srcDir.toFile(), targetDir.toFile());
        }
    }
}
