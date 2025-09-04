package org.openmrs.module.drools.param;

public class DroolsParameterDefinition {

    private String parameterName;

    private DroolsParameterType parameterType;

    private Boolean required = Boolean.TRUE;

    public DroolsParameterDefinition() {

    }

    public DroolsParameterDefinition(String parameterName, DroolsParameterType parameterType, Boolean required) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.required = required;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public DroolsParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(DroolsParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
