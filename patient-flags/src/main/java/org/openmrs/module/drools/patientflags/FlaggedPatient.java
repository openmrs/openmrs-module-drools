package org.openmrs.module.drools.patientflags;

public class FlaggedPatient {
    private Integer patientId;
    private String message;

    private String recommendation;

    public FlaggedPatient(Integer patientId, String message) {
        this.patientId = patientId;
        this.message = message;
    }

    public FlaggedPatient(Integer patientId, String message, String recommendation) {
        this.patientId = patientId;
        this.message = message;
        this.recommendation = recommendation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public String getMessage() {
        return message;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
