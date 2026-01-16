package org.openmrs.module.drools.session.impl;

import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.SessionLease;
import org.openmrs.module.drools.session.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link SessionRegistry} providing thread-safe session
 * management.
 * <p>
 * This implementation uses:
 * <ul>
 * <li>ConcurrentHashMap for thread-safe session storage</li>
 * <li>Per-session ReentrantLock for exclusive session access</li>
 * <li>SessionLease pattern for automatic lock release</li>
 * </ul>
 * <p>
 * Thread Safety: All operations are thread-safe. Multiple threads can safely
 * register, check out, and remove sessions concurrently. Access to individual
 * sessions is serialized through per-session locks.
 */
@Component
public class SessionRegistryImpl implements SessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(SessionRegistryImpl.class);

    /**
     * Thread-safe storage for registered sessions.
     */
    private final ConcurrentHashMap<String, KieSession> sessions = new ConcurrentHashMap<>();

    /**
     * Per-session locks for thread-safe access.
     */
    private final ConcurrentHashMap<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();

    @Override
    public void registerSession(String sessionId, KieSession session) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        sessions.put(sessionId, session);
        sessionLocks.putIfAbsent(sessionId, new ReentrantLock());

        log.info("Registered session in registry: {}", sessionId);
        log.debug("Registry now contains {} session(s)", sessions.size());
    }

    @Override
    public SessionLease checkOutSession(String sessionId, long timeout, TimeUnit unit) throws InterruptedException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        KieSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found in registry: " + sessionId);
        }

        ReentrantLock lock = sessionLocks.get(sessionId);
        if (lock == null) {
            throw new IllegalStateException("Lock not found for session: " + sessionId);
        }

        log.debug("Attempting to check out session: {}", sessionId);

        boolean acquired = lock.tryLock(timeout, unit);
        if (!acquired) {
            throw new RuntimeException("Could not acquire lock for session '" + sessionId
                    + "' within " + timeout + " " + unit.toString().toLowerCase());
        }

        log.debug("Successfully checked out session: {}", sessionId);
        return new SessionLease(session, lock);
    }

    @Override
    public boolean sessionExists(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        return sessions.containsKey(sessionId);
    }

    @Override
    public void removeSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Attempted to remove session with null or empty ID");
            return;
        }

        KieSession session = sessions.remove(sessionId);
        sessionLocks.remove(sessionId);

        if (session != null) {
            log.info("Removed session from registry: {}", sessionId);
            log.debug("Registry now contains {} session(s)", sessions.size());
        } else {
            log.debug("Attempted to remove non-existent session: {}", sessionId);
        }
    }
}
