package org.openmrs.module.drools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;

public class KieContainerBuilder {

    Log log = LogFactory.getLog(KieContainerBuilder.class);

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
                kieFileSystem.write(kieServices.getResources().newClassPathResource(resource.getPath())
                        .setResourceType(resource.getResourceType()));
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
