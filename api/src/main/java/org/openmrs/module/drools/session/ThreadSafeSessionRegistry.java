package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieSession;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe registry for managing stateful KieSession instances.
 * Supports session lifecycle management, metadata tracking, and cleanup operations.
 * 
 * <p>This registry uses a check-out/return pattern to prevent concurrent access issues.
 * Sessions should be checked out using {@link #checkOutSession(String, long, TimeUnit)},
 * used within a try-with-resources block, and automatically returned when the lease closes.</p>
 */
public interface ThreadSafeSessionRegistry {
	
	/**
	 * Registers a KieSession in the registry with metadata.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @param session the KieSession instance to register
	 * @param autoStartable whether the session is auto-startable (started at module initialization)
	 * @return true if registration was successful, false if session already exists
	 */
	boolean registerSession(String sessionId, KieSession session, boolean autoStartable);
	
	/**
	 * Retrieves a KieSession by its identifier.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @return an Optional containing the session if found, empty otherwise
	 */
	Optional<KieSession> getSession(String sessionId);
	
	/**
	 * Checks out a session for exclusive use, blocking if necessary until the lock is available.
	 * Returns a SessionLease that MUST be closed (preferably via try-with-resources) to release the lock.
	 * 
	 * <p>This method prevents Thread1/Thread2 data race conditions by ensuring only one thread
	 * can access the session at a time.</p>
	 * 
	 * <p>Example usage:</p>
	 * <pre>
	 * try (SessionLease lease = registry.checkOutSession("mySession", 5, TimeUnit.SECONDS)) {
	 *     KieSession session = lease.getSession();
	 *     // Perform thread-safe operations on session
	 * } // Lock automatically released
	 * </pre>
	 * 
	 * @param sessionId the unique identifier for the session
	 * @param timeout the maximum time to wait for the lock
	 * @param unit the time unit of the timeout argument
	 * @return a SessionLease that provides access to the session and releases the lock when closed
	 * @throws IllegalArgumentException if session doesn't exist
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws java.util.concurrent.TimeoutException if the lock cannot be acquired within the timeout
	 */
	SessionLease checkOutSession(String sessionId, long timeout, TimeUnit unit) 
			throws InterruptedException, java.util.concurrent.TimeoutException;
	
	/**
	 * Removes a session from the registry and disposes it.
	 * 
	 * @param sessionId the unique identifier for the session to remove
	 * @return true if the session was removed, false if it didn't exist
	 */
	boolean removeSession(String sessionId);
	
	/**
	 * Gets all active session IDs currently in the registry.
	 * 
	 * @return a collection of all active session identifiers
	 */
	Collection<String> getActiveSessionIds();
	
	/**
	 * Gets session IDs for auto-startable sessions only.
	 * 
	 * @return a collection of auto-startable session identifiers
	 */
	Collection<String> getAutoStartableSessionIds();
	
	/**
	 * Checks if a session exists in the registry.
	 * 
	 * @param sessionId the unique identifier to check
	 * @return true if the session exists, false otherwise
	 */
	boolean sessionExists(String sessionId);
	
	/**
	 * Retrieves metadata for a specific session.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @return the session metadata, or null if session doesn't exist
	 */
	SessionMetadata getSessionMetadata(String sessionId);
	
	/**
	 * Cleans up expired or stale sessions based on implementation-specific criteria.
	 * This method is intended to be called periodically or on-demand.
	 */
	void cleanupExpiredSessions();
	
	/**
	 * Gets the total count of active sessions in the registry.
	 * 
	 * @return the number of active sessions
	 */
	long getActiveSessionCount();
}
