package org.openmrs.module.drools.session;

import org.kie.api.runtime.KieSession;

/**
 * Represents a checked-out session lease that ensures exclusive access to a KieSession.
 * Implements AutoCloseable to support try-with-resources pattern for automatic lock release.
 * 
 * <p>This class prevents Thread1/Thread2 data race conditions by ensuring only one thread
 * can access a session at a time. When acquired, the session is locked until the lease
 * is closed (either explicitly or via try-with-resources).</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * try (SessionLease lease = registry.checkOutSession("mySession", 5, TimeUnit.SECONDS)) {
 *     KieSession session = lease.getSession();
 *     // Perform operations on session
 * } // Lock automatically released
 * </pre>
 */
public class SessionLease implements AutoCloseable {
	
	private final KieSession session;
	private final String sessionId;
	private final Runnable releaseCallback;
	private volatile boolean released = false;
	
	/**
	 * Creates a new session lease.
	 * 
	 * @param sessionId the session identifier
	 * @param session the KieSession instance
	 * @param releaseCallback callback to invoke when lease is closed (releases the lock)
	 */
	public SessionLease(String sessionId, KieSession session, Runnable releaseCallback) {
		this.sessionId = sessionId;
		this.session = session;
		this.releaseCallback = releaseCallback;
	}
	
	/**
	 * Gets the KieSession associated with this lease.
	 * 
	 * @return the KieSession instance
	 * @throws IllegalStateException if the lease has already been released
	 */
	public KieSession getSession() {
		if (released) {
			throw new IllegalStateException("Session lease for '" + sessionId + "' has already been released");
		}
		return session;
	}
	
	/**
	 * Gets the session identifier.
	 * 
	 * @return the session ID
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	/**
	 * Releases the session lock. This method is idempotent - calling it multiple times
	 * has no effect after the first call.
	 */
	@Override
	public void close() {
		if (!released) {
			released = true;
			if (releaseCallback != null) {
				releaseCallback.run();
			}
		}
	}
	
	/**
	 * Checks if this lease has been released.
	 * 
	 * @return true if the lease has been released, false otherwise
	 */
	public boolean isReleased() {
		return released;
	}
}
