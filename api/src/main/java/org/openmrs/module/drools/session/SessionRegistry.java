package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieSession;

import java.util.concurrent.TimeUnit;

/**
 * Registry for managing auto-startable Drools sessions.
 * <p>
 * This registry provides thread-safe storage and access to KieSession instances
 * that are configured for auto-start. Sessions registered here are created once
 * at module startup and reused across multiple requests.
 * <p>
 * Thread Safety: All operations are thread-safe. The {@link #checkOutSession}
 * method provides exclusive access to a session using per-session locking.
 */
public interface SessionRegistry {

    /**
     * Registers a KieSession in the registry.
     * <p>
     * This method should be called during module startup for sessions configured
     * with autoStart=true.
     * 
     * @param sessionId the unique identifier for the session
     * @param session   the KieSession instance to register
     */
    void registerSession(String sessionId, KieSession session);

    /**
     * Checks out a session for exclusive use.
     * <p>
     * This method acquires a lock on the specified session and returns a
     * {@link SessionLease} that provides access to the session. The lock is
     * automatically released when the lease is closed.
     * <p>
     * Example usage:
     * 
     * <pre>
     * try (SessionLease lease = registry.checkOutSession("mySession", 5, TimeUnit.SECONDS)) {
     *     KieSession session = lease.getSession();
     *     // Use session safely - no other thread can access it
     * } // Lock automatically released
     * </pre>
     * 
     * @param sessionId the unique identifier of the session to check out
     * @param timeout   the maximum time to wait for the lock
     * @param unit      the time unit of the timeout argument
     * @return a SessionLease providing exclusive access to the session
     * @throws IllegalArgumentException if the session does not exist
     * @throws RuntimeException         if the lock cannot be acquired within the
     *                                  timeout
     * @throws InterruptedException     if the thread is interrupted while waiting
     *                                  for the lock
     */
    SessionLease checkOutSession(String sessionId, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Checks if a session with the given ID exists in the registry.
     * 
     * @param sessionId the unique identifier of the session
     * @return true if the session exists, false otherwise
     */
    boolean sessionExists(String sessionId);

    /**
     * Removes a session from the registry.
     * <p>
     * This method removes the session and its associated lock from the registry.
     * The session itself is not disposed by this method.
     * 
     * @param sessionId the unique identifier of the session to remove
     */
    void removeSession(String sessionId);
}
