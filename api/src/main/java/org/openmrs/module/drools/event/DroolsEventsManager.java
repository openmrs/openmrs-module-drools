package org.openmrs.module.drools.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kie.api.runtime.KieSession;
import org.openmrs.event.Event;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.springframework.stereotype.Component;

@Component
public class DroolsEventsManager {

    private Map<String, List<DroolsSystemEventListener>> activeSubscriptions = new HashMap<>();

    private DaemonToken daemonToken;

    public DroolsEventsManager() {
    }

    public void subscribeSessionEventListenersIfNecessary(String sessionId, KieSession session,
            Map<String, RuleSessionConfig> ruleConfigs) {
        if (!activeSubscriptions.containsKey(sessionId)) {
            RuleSessionConfig sessionConfig = ruleConfigs.get(sessionId);
            if (sessionConfig.getSystemEventListeners() != null) {
                sessionConfig.getSystemEventListeners().forEach(systemEventListener -> {
                    systemEventListener.setSession(session);
                    systemEventListener.setDaemonToken(daemonToken);
                    systemEventListener.getSuscribedActions().forEach(action -> {
                        System.out.println("Subscribing to " + systemEventListener.getSubscribedClass() + " for action "
                                + action.toString());
                        Event.subscribe(systemEventListener.getSubscribedClass(), action.toString(),
                                systemEventListener);
                    });
                });
            }
        }
    }

    public void unsubsribeSessionEventListenersIfNecessary(String sessionId) {
        if (activeSubscriptions.containsKey(sessionId)) {
            activeSubscriptions.get(sessionId).forEach(systemEventListener -> {
                systemEventListener.getSuscribedActions().forEach(action -> {
                    Event.unsubscribe(systemEventListener.getSubscribedClass(), action, systemEventListener);
                });
            });
        }
    }

    public void setDaemonToken(DaemonToken daemonToken) {
        this.daemonToken = daemonToken;
    }

    public DaemonToken getDaemonToken() {
        return daemonToken;
    }
}
