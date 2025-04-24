package org.openmrs.module.drools;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.event.DroolsSystemEventListener;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;

public class TestEventListener extends DroolsSystemEventListener {

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public List<Event.Action> getSubscribedActions() {
        return List.of(Event.Action.CREATED, Event.Action.UPDATED);
    }

    @Override
    public Class<?> getSubscribedClass() {
        return Patient.class;
    }

    @Override
    public void processMessage(KieSession session, Message message) {
        if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            try {
                String patientUuid = mapMessage.getString("uuid");
                log.debug("Received patient UUID: " + patientUuid);

                Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
                if (patient != null) {
                    log.debug("Patient found: " + patient);
                    // Process the patient object as needed
                    session.insert(patient);
                    session.fireAllRules();
                    log.debug("Rules fired for patient: " + patient);
                } else {
                    log.warn("Patient not found for UUID: " + patientUuid);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
