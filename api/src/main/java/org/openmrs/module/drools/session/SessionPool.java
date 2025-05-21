package org.openmrs.module.drools.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.DroolsConfig;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

// TODO: Do we need a session pool? If so, re-specify component
// @Component
public class SessionPool {
    private final Log log = LogFactory.getLog(this.getClass());

    // @Autowired
    private DroolsConfig droolsConfig;
    private boolean isInitialized;
    private final Map<String, Queue<KieSession>> sessionPool = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
    private final Map<String, RuleSessionConfig> sessionConfigs = new ConcurrentHashMap<>();

    public SessionPool() {
    }

    private void initialize(List<RuleProvider> ruleProviders, KieContainer kieContainer) {
        for (RuleProvider provider : ruleProviders) {
            for (RuleSessionConfig config : provider.getSessionConfigs()) {
                sessionConfigs.put(config.getSessionId(), config);
                sessionPool.put(config.getSessionId(), new LinkedBlockingQueue<>());
                sessionLocks.put(config.getSessionId(), new ReentrantLock());

                // Pre-warm the pool with initial sessions
                 for (int i = 0; i < config.getInitialPoolSize(); i++) {
                     KieSession session = createNewSession(config.getSessionId(), kieContainer);
                     sessionPool.get(config.getSessionId()).add(session);
                 }
            }
        }
        isInitialized = true;
    }

    public KieSession borrowSession(String sessionId, KieContainer kieContainer) {
        if (!isInitialized) {
            initialize(droolsConfig.getRuleProviders(), kieContainer);
        }
        Queue<KieSession> sessions = sessionPool.get(sessionId);
        ReentrantLock lock = sessionLocks.get(sessionId);

        lock.lock();
        try {
            if (sessions != null && !sessions.isEmpty()) {
                return sessions.poll();
            } else {
                return createNewSession(sessionId, kieContainer);
            }
        } finally {
            lock.unlock();
        }
    }

    public void returnSession(String sessionId, KieSession session) {
        RuleSessionConfig config = sessionConfigs.get(sessionId);
        Queue<KieSession> sessions = sessionPool.get(sessionId);
        ReentrantLock lock = sessionLocks.get(sessionId);

        if (sessions == null || config == null || lock == null) {
            log.error("Cannot return session. Missing config, pool, or lock for sessionId: " + sessionId);
            throw new DroolsSessionException("Session configuration not found for sessionId: " + sessionId);
        }

        log.debug("Returning KieSession for sessionId: " + sessionId);

        lock.lock();
        try {
            // Clear session state by removing all facts
            session.getFactHandles().forEach(session::delete);
            sessions.add(session);
        } finally {
            lock.unlock();
        }

    }

    public void disposeAll() {
        sessionPool.values().forEach(queue -> queue.forEach(KieSession::dispose));
        sessionPool.clear();
    }

    private KieSession createNewSession(String sessionId, KieContainer kieContainer) {
        return CommonUtils.createKieSession(kieContainer, sessionConfigs.get(sessionId),
                droolsConfig.getExternalEvaluatorManager());
    }

    public DroolsConfig getDroolsConfig() {
        return droolsConfig;
    }

    public void setDroolsConfig(DroolsConfig droolsConfig) {
        this.droolsConfig = droolsConfig;
    }
}
