package org.openmrs.module.drools;

import org.openmrs.Patient;

public class PatientFlag {
    private Patient patient;
    private String type;
    private String severity;
    private String message;

    public PatientFlag() {
    }

    public PatientFlag(Patient patient, String type) {
        this.patient = patient;
        this.type = type;
    }

    public PatientFlag(Patient patient, String type, String severity, String message) {
        this.patient = patient;
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PatientFlag that = (PatientFlag) obj;
        if (patient != null ? !patient.equals(that.patient) : that.patient != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (severity != null ? !severity.equals(that.severity) : that.severity != null) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

}
