package org.openmrs.module.drools.session.impl;

import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.SessionLease;
import org.openmrs.module.drools.session.SessionMetadata;
import org.openmrs.module.drools.session.ThreadSafeSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Thread-safe registry implementation for managing stateful KieSession instances.
 * 
 * <p>Uses a check-out/return pattern with ReentrantLock to prevent Thread1/Thread2
 * data race conditions. Sessions must be checked out via {@link #checkOutSession(String, long, TimeUnit)}
 * which returns a SessionLease that MUST be closed to release the lock.</p>
 * 
 * <p>Leverages Drools 6+ native thread safety in combination with explicit locking
 * for inter-operation coordination.</p>
 */
@Component
public class ThreadSafeSessionRegistryImpl implements ThreadSafeSessionRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(ThreadSafeSessionRegistryImpl.class);
	
	private static final long MAX_IDLE_HOURS = 24;
	
	// Session storage
	private final ConcurrentHashMap<String, KieSession> sessions = new ConcurrentHashMap<>();
	
	// Session metadata
	private final ConcurrentHashMap<String, SessionMetadata> metadata = new ConcurrentHashMap<>();
	
	// Session locks for check-out mechanism
	private final ConcurrentHashMap<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();
	
	private final AtomicLong registrationCounter = new AtomicLong(0);
	
	@Override
	public boolean registerSession(String sessionId, KieSession session, boolean autoStartable) {
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
		
		// Create and store metadata
		SessionMetadata meta = new SessionMetadata(sessionId, autoStartable);
		metadata.put(sessionId, meta);
		
		long count = registrationCounter.incrementAndGet();
		log.info("Registered session '{}' (autoStartable={}) - Total registrations: {}", 
				sessionId, autoStartable, count);
		return true;
	}
	
	@Override
	public Optional<KieSession> getSession(String sessionId) {
		if (sessionId == null) {
			return Optional.empty();
		}
		
		KieSession session = sessions.get(sessionId);
		if (session != null) {
			SessionMetadata meta = metadata.get(sessionId);
			if (meta != null) {
				meta.updateLastAccessed();
			}
			log.debug("Retrieved session '{}' from registry", sessionId);
		} else {
			log.debug("Session '{}' not found in registry", sessionId);
		}
		
		return Optional.ofNullable(session);
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
		
		// Update metadata
		SessionMetadata meta = metadata.get(sessionId);
		if (meta != null) {
			meta.updateLastAccessed();
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
	public boolean removeSession(String sessionId) {
		if (sessionId == null) {
			return false;
		}
		
		// Remove session and its associated resources
		KieSession session = sessions.remove(sessionId);
		metadata.remove(sessionId);
		sessionLocks.remove(sessionId); // Clean up the lock too
		
		if (session != null) {
			try {
				// Dispose the session
				session.dispose();
				log.info("Removed and disposed session '{}'", sessionId);
				return true;
			} catch (Exception e) {
				log.error("Error disposing session '{}': {}", sessionId, e.getMessage(), e);
				return true; // Session was removed even if disposal failed
			}
		}
		
		log.debug("Session '{}' not found for removal", sessionId);
		return false;
	}
	
	@Override
	public Collection<String> getActiveSessionIds() {
		return sessions.keySet().stream().collect(Collectors.toList());
	}
	
	@Override
	public Collection<String> getAutoStartableSessionIds() {
		return metadata.values().stream()
				.filter(SessionMetadata::isAutoStartable)
				.map(SessionMetadata::getSessionId)
				.collect(Collectors.toList());
	}
	
	@Override
	public boolean sessionExists(String sessionId) {
		return sessionId != null && sessions.containsKey(sessionId);
	}
	
	@Override
	public SessionMetadata getSessionMetadata(String sessionId) {
		return sessionId != null ? metadata.get(sessionId) : null;
	}
	
	/**
	 * Automated cleanup of expired sessions.
	 * Runs every hour to remove sessions idle for more than 24 hours.
	 * Uses consistent log.debug() for automated operations.
	 */
	@Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
	@Override
	public void cleanupExpiredSessions() {
		LocalDateTime cutoffTime = LocalDateTime.now().minus(MAX_IDLE_HOURS, ChronoUnit.HOURS);
		int cleanedCount = 0;
		
		log.debug("Starting automated expired session cleanup (cutoff: {})", cutoffTime);
		
		for (SessionMetadata meta : metadata.values()) {
			if (meta.getLastAccessed().isBefore(cutoffTime)) {
				String sessionId = meta.getSessionId();
				if (removeSession(sessionId)) {
					cleanedCount++;
					log.debug("Cleaned up expired session '{}' (last accessed: {})", 
				sessionId, meta.getLastAccessed());
			}
		}
	}
	
	if (cleanedCount > 0) {
		log.info("Automated cleanup completed: removed {} expired session(s)", cleanedCount);
	} else {
		log.debug("Automated cleanup completed: no expired sessions found");
	}
}	@Override
	public long getActiveSessionCount() {
		return sessions.size();
	}
}
