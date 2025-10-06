package org.openmrs.module.drools.session.impl;

import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.SessionMetadata;
import org.openmrs.module.drools.session.ThreadSafeSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe registry implementation for managing stateful KieSession instances.
 * Leverages Drools 6+ native thread safety and ConcurrentHashMap for concurrent access.
 */
@Component
public class ThreadSafeSessionRegistryImpl implements ThreadSafeSessionRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(ThreadSafeSessionRegistryImpl.class);
	
	private static final long MAX_IDLE_HOURS = 24;
	
	private final ConcurrentHashMap<String, KieSession> sessions = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, SessionMetadata> metadata = new ConcurrentHashMap<>();
	
	private final AtomicLong registrationCounter = new AtomicLong(0);
	
	@Override
	public void registerSession(String sessionId, KieSession session, boolean autoStartable) {
		if (sessionId == null || sessionId.trim().isEmpty()) {
			throw new IllegalArgumentException("Session ID cannot be null or empty");
		}
		if (session == null) {
			throw new IllegalArgumentException("KieSession cannot be null");
		}
		
		KieSession existingSession = sessions.putIfAbsent(sessionId, session);
		if (existingSession != null) {
			log.warn("Session with ID '{}' already exists in registry. Registration ignored.", sessionId);
			throw new IllegalArgumentException("Session with ID '" + sessionId + "' already exists");
		}
		
		SessionMetadata meta = new SessionMetadata(sessionId, autoStartable);
		metadata.put(sessionId, meta);
		
		long count = registrationCounter.incrementAndGet();
		log.info("Registered session '{}' (autoStartable={}) - Total registrations: {}", 
				sessionId, autoStartable, count);
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
	public boolean removeSession(String sessionId) {
		if (sessionId == null) {
			return false;
		}
		
		KieSession session = sessions.remove(sessionId);
		metadata.remove(sessionId);
		
		if (session != null) {
			try {
				session.dispose();
				log.info("Removed and disposed session '{}'", sessionId);
				return true;
			} catch (Exception e) {
				log.error("Error disposing session '{}': {}", sessionId, e.getMessage(), e);
				return true;
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
	
	@Override
	public void cleanupExpiredSessions() {
		LocalDateTime cutoffTime = LocalDateTime.now().minus(MAX_IDLE_HOURS, ChronoUnit.HOURS);
		int cleanedCount = 0;
		
		log.debug("Starting expired session cleanup (cutoff: {})", cutoffTime);
		
		for (SessionMetadata meta : metadata.values()) {
			if (meta.getLastAccessed().isBefore(cutoffTime)) {
				String sessionId = meta.getSessionId();
				if (removeSession(sessionId)) {
					cleanedCount++;
					log.info("Cleaned up expired session '{}' (last accessed: {})", 
							sessionId, meta.getLastAccessed());
				}
			}
		}
		
		if (cleanedCount > 0) {
			log.info("Cleanup completed: removed {} expired session(s)", cleanedCount);
		} else {
			log.debug("Cleanup completed: no expired sessions found");
		}
	}
	
	@Override
	public long getActiveSessionCount() {
		return sessions.size();
	}
}
