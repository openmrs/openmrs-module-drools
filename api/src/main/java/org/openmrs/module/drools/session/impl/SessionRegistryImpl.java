package org.openmrs.module.drools.session.impl;

import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.SessionLease;
import org.openmrs.module.drools.session.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe registry implementation for managing stateful KieSession instances.
 * 
 * <p>Uses a check-out/return pattern with ReentrantLock to prevent Thread1/Thread2
 * data race conditions. Sessions must be checked out via {@link #checkOutSession(String, long, TimeUnit)}
 * which returns a SessionLease that MUST be closed to release the lock.</p>
 */
@Component
public class SessionRegistryImpl implements SessionRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(SessionRegistryImpl.class);
	
	// Session storage
	private final ConcurrentHashMap<String, KieSession> sessions = new ConcurrentHashMap<>();
	
	// Session locks for check-out mechanism
	private final ConcurrentHashMap<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
	
	@Override
	public boolean registerSession(String sessionId, KieSession session) {
		if (sessionId == null || sessionId.trim().isEmpty()) {
			log.debug("Registration failed: Session ID cannot be null or empty");
			return false;
		}
		if (session == null) {
			log.debug("Registration failed: KieSession cannot be null");
			return false;
		}
		
		// Attempt to register session atomically
		KieSession existingSession = sessions.putIfAbsent(sessionId, session);
		if (existingSession != null) {
			log.debug("Session with ID '{}' already exists in registry. Registration ignored.", sessionId);
			return false;
		}
		
		// Create lock for this session
		sessionLocks.putIfAbsent(sessionId, new ReentrantLock(true)); // fair lock
		
		log.info("Registered session '{}'", sessionId);
		return true;
	}
	
	@Override
	public SessionLease checkOutSession(String sessionId, long timeout, TimeUnit unit) 
			throws InterruptedException, TimeoutException {
		if (sessionId == null) {
			throw new IllegalArgumentException("Session ID cannot be null");
		}
		
		// Verify session exists
		KieSession session = sessions.get(sessionId);
		if (session == null) {
			throw new IllegalArgumentException("Session '" + sessionId + "' not found in registry");
		}
		
		// Get the lock for this session
		ReentrantLock lock = sessionLocks.get(sessionId);
		if (lock == null) {
			throw new IllegalStateException("Lock not found for session '" + sessionId + "'");
		}
		
		// Try to acquire the lock with timeout
		boolean acquired = lock.tryLock(timeout, unit);
		if (!acquired) {
			throw new TimeoutException("Could not acquire lock for session '" + sessionId + 
					"' within " + timeout + " " + unit);
		}
		
		log.debug("Checked out session '{}' (lock acquired by thread '{}')", 
				sessionId, Thread.currentThread().getName());
		
		// Return lease with callback to release lock
		return new SessionLease(sessionId, session, () -> returnSession(sessionId, lock));
	}
	
	/**
	 * Internal method to return (unlock) a session.
	 * Called automatically by SessionLease.close().
	 */
	private void returnSession(String sessionId, ReentrantLock lock) {
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
			log.debug("Returned session '{}' (lock released by thread '{}')", 
					sessionId, Thread.currentThread().getName());
		} else {
			log.warn("Attempted to return session '{}' but lock not held by current thread", sessionId);
		}
	}
	
	@Override
	public boolean sessionExists(String sessionId) {
		return sessionId != null && sessions.containsKey(sessionId);
	}
	
	@Override
	public void cleanupExpiredSessions() {
		// Manual cleanup method - can be called when needed
		// Implementation can be added later if specific cleanup logic is required
		log.debug("Manual cleanup invoked - no automatic cleanup configured");
	}
}
