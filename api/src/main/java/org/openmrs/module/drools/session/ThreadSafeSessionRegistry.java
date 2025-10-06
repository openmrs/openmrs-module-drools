package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieSession;

import java.util.Collection;
import java.util.Optional;

/**
 * Thread-safe registry for managing stateful KieSession instances.
 * Supports session lifecycle management, metadata tracking, and cleanup operations.
 */
public interface ThreadSafeSessionRegistry {
	
	/**
	 * Registers a KieSession in the registry with metadata.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @param session the KieSession instance to register
	 * @param autoStartable whether the session is auto-startable (started at module initialization)
	 * @throws IllegalArgumentException if sessionId or session is null, or if sessionId already exists
	 */
	void registerSession(String sessionId, KieSession session, boolean autoStartable);
	
	/**
	 * Retrieves a KieSession by its identifier.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @return an Optional containing the session if found, empty otherwise
	 */
	Optional<KieSession> getSession(String sessionId);
	
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
