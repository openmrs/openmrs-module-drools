package org.openmrs.module.drools.session;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe metadata for tracking KieSession lifecycle and access patterns.
 */
public class SessionMetadata {
	
	private final String sessionId;
	
	private final boolean autoStartable;
	
	private final LocalDateTime createdAt;
	
	private final String createdByThread;
	
	private volatile LocalDateTime lastAccessed;
	
	private final AtomicInteger accessCount;
	
	public SessionMetadata(String sessionId, boolean autoStartable) {
		this.sessionId = sessionId;
		this.autoStartable = autoStartable;
		this.createdAt = LocalDateTime.now();
		this.createdByThread = Thread.currentThread().getName();
		this.lastAccessed = LocalDateTime.now();
		this.accessCount = new AtomicInteger(0);
	}
	
	public void updateLastAccessed() {
		this.lastAccessed = LocalDateTime.now();
		this.accessCount.incrementAndGet();
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public boolean isAutoStartable() {
		return autoStartable;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public String getCreatedByThread() {
		return createdByThread;
	}
	
	public LocalDateTime getLastAccessed() {
		return lastAccessed;
	}
	
	public int getAccessCount() {
		return accessCount.get();
	}
	
	@Override
	public String toString() {
		return "SessionMetadata{" +
				"sessionId='" + sessionId + '\'' +
				", autoStartable=" + autoStartable +
				", createdAt=" + createdAt +
				", createdByThread='" + createdByThread + '\'' +
				", lastAccessed=" + lastAccessed +
				", accessCount=" + accessCount.get() +
				'}';
	}
}
