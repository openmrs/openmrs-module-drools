package org.openmrs.module.drools.descriptor;

import java.util.List;

public class SessionConfigDescriptor {
    private String sessionId;

    private String agendaGroup;
    private List<ParamDescriptor> params;
    private String returnObjectsTypeClassName;

    public SessionConfigDescriptor() {

    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAgendaGroup() {
        return agendaGroup;
    }

    public void setAgendaGroup(String agendaGroup) {
        this.agendaGroup = agendaGroup;
    }

    public List<ParamDescriptor> getParams() {
        return params;
    }

    public void setParams(List<ParamDescriptor> params) {
        this.params = params;
    }

    public String getReturnObjectsTypeClassName() {
        return returnObjectsTypeClassName;
    }

    public void setReturnObjectsTypeClassName(String returnObjectsTypeClassName) {
        this.returnObjectsTypeClassName = returnObjectsTypeClassName;
    }
}