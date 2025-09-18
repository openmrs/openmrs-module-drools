package org.openmrs.module.drools.descriptor;

public class RuleDescriptor {
    private String name;
    private String path;

    public RuleDescriptor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
