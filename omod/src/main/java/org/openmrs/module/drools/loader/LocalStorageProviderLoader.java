package org.openmrs.module.drools.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.DroolsModuleConstants;
import org.openmrs.module.drools.provider.JsonBackedRuleProvider;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.descriptor.ProviderDescriptor;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LocalStorageProviderLoader implements RuleProviderLoader {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<RuleProvider> loadRuleProviders() {
        Path rootDir = getRootDir();
        if (rootDir == null) {
            log.warn("Drools configuration directory does not exist");
            return Collections.emptyList();
        }
        return loadProviders(rootDir);
    }

    private List<RuleProvider> loadProviders(Path rootDir) {
        List<RuleProvider> providers = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(rootDir)) {
            pathStream.filter(p -> p.getFileName().toString().equals("provider.json"))
                    .forEach(p -> {
                        try {
                            ProviderDescriptor cfg = mapper.readValue(p.toFile(), ProviderDescriptor.class);
                            providers.add(new JsonBackedRuleProvider(cfg, p.getParent()));
                        } catch (IOException e) {
                            log.error("Error processing provider", e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to traverse directory: " + rootDir, e);
        }
        return providers;
    }

    private Path getRootDir() {
        Path omrsAppDir = Paths.get(OpenmrsUtil.getApplicationDataDirectory());
        String droolsConfigDir = Context.getAdministrationService().getGlobalProperty(DroolsModuleConstants.GP_LOCAL_STORAGE_DIR);
        Path droolsConfigStoragePath = omrsAppDir.resolve(droolsConfigDir);

        if (Files.exists(droolsConfigStoragePath)) {
            return droolsConfigStoragePath;
        } else {
            // TODO: Handle edge cases
            return null;
        }
    }
}
