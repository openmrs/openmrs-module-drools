package org.openmrs.module.drools.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kie.api.runtime.KieSession;
import org.openmrs.event.Event;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.drools.session.DroolsSessionConfig;

public class DroolsEventsManager {

    private Map<String, List<DroolsSystemEventListener>> activeSubscriptions = new HashMap<>();

    private static DaemonToken daemonToken;

    public DroolsEventsManager() {
    }

    public void subscribeSessionEventListenersIfNecessary(String sessionId, KieSession session,
            Map<String, DroolsSessionConfig> ruleConfigs) {
        if (!activeSubscriptions.containsKey(sessionId)) {
            DroolsSessionConfig sessionConfig = ruleConfigs.get(sessionId);
            if (sessionConfig.getSystemEventListeners() != null) {
                sessionConfig.getSystemEventListeners().forEach(systemEventListener -> {
                    systemEventListener.setSession(session);
                    systemEventListener.setDaemonToken(daemonToken);
                    systemEventListener.getSubscribedActions().forEach(action -> {
                        Event.subscribe(systemEventListener.getSubscribedClass(), action.toString(),
                                systemEventListener);
                    });
                });
            }
        }
    }

    public void unsubscribeSessionEventListenersIfNecessary(String sessionId) {
        if (activeSubscriptions.containsKey(sessionId)) {
            activeSubscriptions.get(sessionId).forEach(systemEventListener -> {
                systemEventListener.getSubscribedActions().forEach(action -> {
                    Event.unsubscribe(systemEventListener.getSubscribedClass(), action, systemEventListener);
                });
            });
        }
    }

    public static void setDaemonToken(DaemonToken token) {
        daemonToken = token;
    }

}
