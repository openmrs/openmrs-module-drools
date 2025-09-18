package org.openmrs.module.drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KieContainerBuilder {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private KieServices kieServices;

    private KieFileSystem kieFileSystem;

    private Set<RuleResource> resources;

    private KieContainer kieContainer;

    public KieContainerBuilder(KieServices kieServices, KieFileSystem kieFileSystem) {
        this.kieServices = kieServices;
        this.kieFileSystem = kieFileSystem;
        this.resources = new HashSet<>();
    }

    public KieContainerBuilder(KieServices kieServices, KieFileSystem kieFileSystem, List<RuleResource> resources) {
        this.kieServices = kieServices;
        this.kieFileSystem = kieFileSystem;
        this.resources = new HashSet<>(resources);
    }

    public KieContainer build() {
        if (kieContainer != null) {
            return kieContainer;
        }
        for (RuleResource resource : resources) {
            try {
                File file = new File(resource.getPath());
                if (file.exists()) {
                    kieFileSystem.write(kieServices.getResources()
                            .newFileSystemResource(file)
                            .setResourceType(resource.getResourceType()));
                } else {
                    // Fallback to classpath
                    kieFileSystem.write(kieServices.getResources()
                            .newClassPathResource(resource.getPath())
                            .setResourceType(resource.getResourceType()));
                }

            } catch (Exception e) {
                log.error("Error while adding resource: " + resource.getPath(), e);
            }
        }
        kieServices.newKieBuilder(kieFileSystem).buildAll();
        this.kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        return kieContainer;
    }

    public KieContainerBuilder addResource(RuleResource resource) {
        if (resource != null) {
            this.resources.add(resource);
        }
        return this;
    }

    public KieContainerBuilder addResources(List<RuleResource> resources) {
        if (resources != null) {
            this.resources.addAll(resources);
        }
        return this;
    }
}
