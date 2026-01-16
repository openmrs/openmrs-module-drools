package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieSession;

import java.util.concurrent.locks.Lock;

/**
 * A lease for thread-safe access to a KieSession.
 * <p>
 * This class provides exclusive access to a Drools session by holding a lock
 * that is automatically released when the lease is closed. It should be used
 * in a try-with-resources block to ensure proper lock release.
 * <p>
 * Example usage:
 * 
 * <pre>
 * try (SessionLease lease = registry.checkOutSession("mySession", 5, TimeUnit.SECONDS)) {
 *     KieSession session = lease.getSession();
 *     session.insert(fact);
 *     session.fireAllRules();
 * } // Lock automatically released here
 * </pre>
 * <p>
 * Thread Safety: This class ensures that only one thread can access a session
 * at a time, preventing race conditions in Drools session operations.
 */
public class SessionLease implements AutoCloseable {

    private final KieSession session;

    private final Lock lock;

    /**
     * Creates a new SessionLease.
     * <p>
     * Note: The lock should already be acquired before creating this lease.
     * 
     * @param session the KieSession to provide access to
     * @param lock    the lock that protects access to this session
     */
    public SessionLease(KieSession session, Lock lock) {
        this.session = session;
        this.lock = lock;
    }

    /**
     * Gets the KieSession associated with this lease.
     * 
     * @return the KieSession
     */
    public KieSession getSession() {
        return session;
    }

    /**
     * Releases the lock held by this lease.
     * <p>
     * This method is automatically called when used in a try-with-resources block.
     */
    @Override
    public void close() {
        lock.unlock();
    }
}
