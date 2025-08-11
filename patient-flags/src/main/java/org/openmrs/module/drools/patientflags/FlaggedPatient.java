package org.openmrs.module.drools.patientflags;

public class FlaggedPatient {
    private Integer patientId;
    private String message;

    public FlaggedPatient(Integer patientId, String message) {
        this.patientId = patientId;
        this.message = message;
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
