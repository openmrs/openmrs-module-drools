package org.openmrs.module.drools.descriptor;

public class ParamDescriptor {

    private String name;
    private String type;
    private boolean required;

    public ParamDescriptor() {

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
