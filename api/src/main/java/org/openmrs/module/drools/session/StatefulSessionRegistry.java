package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.DroolsConfig;
import org.openmrs.module.drools.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatefulSessionRegistry {

    @Autowired
    private DroolsConfig droolsConfig;

    private final Map<String, KieSession> sessions = new ConcurrentHashMap<>();

    public StatefulSessionRegistry() {

    }

    /**
     * Retrieves an existing Drools session by ID.
     * 
     * @param sessionId the identifier of the session to retrieve
     * @return KieSession the requested Drools session
     * @throws DroolsSessionException if the session with the specified ID does not
     *                                exist
     * @see #requestSession(RuleSessionConfig, KieContainer) for creating a new
     *      session
     */
    public KieSession getSession(String sessionId) {
        KieSession session = sessions.get(sessionId);
        if (session == null) {
            throw new DroolsSessionException("Session with ID " + sessionId + " does not exist");
        }
        return session;
    }

    /**
     * Retrieves an existing session or creates a new Drools session if it doesn't
     * exist.
     * 
     * @param config       the configuration for the session
     * @param kieContainer the KIE container to use for creating a new session
     * @param globalBindings all session(s) globals
     * @return KieSession the existing or newly created Drools session
     * @throws DroolsSessionException if the session configuration is invalid or the
     *                                session could not be created
     */
    public KieSession requestSession(RuleSessionConfig config, KieContainer kieContainer, Map<String, Map <String, Object>> globalBindings) {
        KieSession session = sessions.get(config.getSessionId());
        if (session != null) {
            return session;
        }
        session = CommonUtils.createKieSession(kieContainer, config, droolsConfig.getExternalEvaluatorManager(), globalBindings);

        sessions.put(config.getSessionId(), session);
        return session;
    }

    public KieSession addSession(String sessionId, KieSession session) {
        sessions.put(sessionId, session);
        return session;
    }

    public void disposeAll() {
        sessions.values().forEach(KieSession::dispose);
        sessions.clear();
    }

    public DroolsConfig getDroolsConfig() {
        return droolsConfig;
    }

    public void setDroolsConfig(DroolsConfig droolsConfig) {
        this.droolsConfig = droolsConfig;
    }
}
