package org.openmrs.module.drools.session;

import java.util.List;

public class DroolsExecutionResult {
    private String sessionId;
    private int firedRulesCount;
    private List<Object> results;

    public DroolsExecutionResult() {

    }

    public DroolsExecutionResult(String sessionId, int firedRulesCount, List<Object> results) {
        this.sessionId = sessionId;
        this.firedRulesCount = firedRulesCount;
        this.results = results;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getFiredRulesCount() {
        return firedRulesCount;
    }

    public void setFiredRulesCount(int firedRulesCount) {
        this.firedRulesCount = firedRulesCount;
    }

    public List<Object> getResults() {
        return results;
    }

    public void setResults(List<Object> results) {
        this.results = results;
    }
}
