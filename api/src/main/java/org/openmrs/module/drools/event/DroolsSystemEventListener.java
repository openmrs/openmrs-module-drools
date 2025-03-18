package org.openmrs.module.drools.event;

import java.util.List;

import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.runtime.KieSession;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;

public abstract class DroolsSystemEventListener implements EventListener {
    private Log log = LogFactory.getLog(this.getClass());

    private DaemonToken daemonToken;

    private KieSession session;

    abstract public List<Event.Action> getSuscribedActions();

    abstract public Class<?> getSubscribedClass();

    abstract public void processMessage(KieSession session, Message message);

    @Override
    public void onMessage(Message message) {
        log.debug(String.format("Received message: \n%s", message));

        try {
            Daemon.runInDaemonThread(() -> {
                try {
                    processMessage(session, message);
                } catch (Exception e) {
                    log.error("Error processing message", e);
                }
            }, daemonToken);
        } catch (Exception e) {
            log.error(String.format("Failed to start Daemon thread to process message!\n%s", message.toString()), e);
        }
    }

    public void setSession(KieSession session) {
        this.session = session;
    }

    public KieSession getSession() {
        return session;
    }

    public void setDaemonToken(DaemonToken daemonToken) {
        this.daemonToken = daemonToken;
    }

    public DaemonToken getDaemonToken() {
        return daemonToken;
    }
}
