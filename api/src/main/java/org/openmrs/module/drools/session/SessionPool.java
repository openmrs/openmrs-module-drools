package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.DroolsConfig;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.utils.CommonUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

// TODO: fix component
public class SessionPool {
    private DroolsConfig droolsConfig;
    private final Map<String, Queue<KieSession>> sessionPool = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
    private final Map<String, RuleSessionConfig> sessionConfigs = new ConcurrentHashMap<>();

    public SessionPool() {
    }

    public SessionPool(DroolsConfig droolsConfig) {
        this.droolsConfig = droolsConfig;
        initialize(droolsConfig.getRuleProviders());
    }

    private void initialize(List<RuleProvider> ruleProviders) {
        for (RuleProvider provider : ruleProviders) {
            for (RuleSessionConfig config : provider.getSessionConfigs()) {
                sessionConfigs.put(config.getSessionId(), config);
                sessionPool.put(config.getSessionId(), new LinkedBlockingQueue<>());
                sessionLocks.put(config.getSessionId(), new ReentrantLock());

                // Pre-warm the pool with initial sessions
                // for (int i = 0; i < config.getInitialPoolSize(); i++) {
                // KieSession session = createNewSession(config.getSessionId());
                // sessionPool.get(config.getSessionId()).add(session);
                // }
            }
        }
    }

    public KieSession borrowSession(String sessionId, KieContainer kieContainer) {
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

        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        if (config != null && sessions != null) {
            lock.lock();
            try {
                // FIXME: should we use stateless sessions by default within the pool?
                if (config.getStateful()) {
                    // Reset working memory for stateful sessions
                    session.dispose();
                }
                // Return to pool
                sessions.add(session);
            } finally {
                lock.unlock();
            }
        } else {
            // Dispose if sessionId is invalid
            session.dispose();
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
