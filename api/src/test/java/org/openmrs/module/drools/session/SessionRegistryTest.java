package org.openmrs.module.drools.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.drools.session.impl.SessionRegistryImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SessionRegistry implementation.
 * Verifies thread-safe session management functionality.
 */
public class SessionRegistryTest {

    private SessionRegistry registry;

    @Mock
    private KieSession mockSession1;

    @Mock
    private KieSession mockSession2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registry = new SessionRegistryImpl();
    }

    @Test
    public void testRegisterSession_Success() {
        // Test successful registration
        boolean result = registry.registerSession("test-session-1", mockSession1);

        assertTrue(result);
        assertTrue(registry.sessionExists("test-session-1"));
    }

    @Test
    public void testRegisterSession_ReturnsFalseForNullSessionId() {
        // Test null session ID returns false
        boolean result = registry.registerSession(null, mockSession1);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForEmptySessionId() {
        // Test empty session ID returns false
        boolean result = registry.registerSession("", mockSession1);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForNullSession() {
        // Test null KieSession returns false
        boolean result = registry.registerSession("test-session", null);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForDuplicateSessionId() {
        // Test duplicate session ID returns false
        boolean first = registry.registerSession("test-session", mockSession1);
        boolean second = registry.registerSession("test-session", mockSession2);
        
        assertTrue(first);
        assertFalse(second);
    }

    @Test
    public void testSessionExists_ReturnsTrueWhenExists() {
        // Test session existence check
        registry.registerSession("test-session", mockSession1);

        assertTrue(registry.sessionExists("test-session"));
    }

    @Test
    public void testSessionExists_ReturnsFalseWhenNotExists() {
        // Test non-existent session
        assertFalse(registry.sessionExists("non-existent"));
    }

    @Test
    public void testSessionExists_ReturnsFalseForNullSessionId() {
        // Test null session ID
        assertFalse(registry.sessionExists(null));
    }

    @Test
    public void testCheckOutSession_Success() throws Exception {
        // Test successful session check-out
        registry.registerSession("test-session", mockSession1);

        try (SessionLease lease = 
                registry.checkOutSession("test-session", 5, java.util.concurrent.TimeUnit.SECONDS)) {
            
            assertNotNull(lease);
            assertEquals("test-session", lease.getSessionId());
            assertSame(mockSession1, lease.getSession());
            assertFalse(lease.isReleased());
        }
    }

    @Test
    public void testCheckOutSession_ThrowsExceptionForNonExistentSession() {
        // Test checking out non-existent session throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            registry.checkOutSession("non-existent", 5, java.util.concurrent.TimeUnit.SECONDS);
        });
    }

    @Test
    public void testCheckOutSession_PreventsDataRace() throws Exception {
        // Test that check-out mechanism prevents concurrent access using multiple threads
        registry.registerSession("test-session", mockSession1);

        // Use a CountDownLatch to coordinate threads
        final java.util.concurrent.CountDownLatch latch1 = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch latch2 = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicBoolean thread2TimedOut = new java.util.concurrent.atomic.AtomicBoolean(false);

        // Thread 1: Acquire lock and hold it
        Thread thread1 = new Thread(() -> {
            try (SessionLease lease = 
                    registry.checkOutSession("test-session", 5, java.util.concurrent.TimeUnit.SECONDS)) {
                
                assertNotNull(lease);
                latch1.countDown(); // Signal that thread 1 has acquired the lock
                
                // Wait for thread 2 to attempt and fail
                latch2.await(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Thread 2: Try to acquire same lock (should timeout)
        Thread thread2 = new Thread(() -> {
            try {
                latch1.await(2, java.util.concurrent.TimeUnit.SECONDS); // Wait for thread 1 to acquire
                
                // Try to acquire - should timeout
                registry.checkOutSession("test-session", 100, java.util.concurrent.TimeUnit.MILLISECONDS);
                
            } catch (java.util.concurrent.TimeoutException e) {
                // Expected - lock held by thread 1
                thread2TimedOut.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch2.countDown();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join(5000);
        thread2.join(5000);

        assertTrue(thread2TimedOut.get(), "Thread 2 should have timed out waiting for lock");
        
        // After threads complete, should be able to check out again
        try (SessionLease lease = 
                registry.checkOutSession("test-session", 1, java.util.concurrent.TimeUnit.SECONDS)) {
            assertNotNull(lease);
        }
    }

    @Test
    public void testCleanupExpiredSessions_DoesNotThrow() {
        // Test that cleanup method can be called without errors
        registry.registerSession("test-session", mockSession1);
        
        assertDoesNotThrow(() -> registry.cleanupExpiredSessions());
        
        // Session should still exist (no automatic cleanup)
        assertTrue(registry.sessionExists("test-session"));
    }

    @Test
    public void testMultipleOperations_MaintainsConsistency() throws Exception {
        // Test multiple operations maintain consistency
        registry.registerSession("session-1", mockSession1);
        registry.registerSession("session-2", mockSession2);

        assertTrue(registry.sessionExists("session-1"));
        assertTrue(registry.sessionExists("session-2"));

        try (SessionLease lease = 
                registry.checkOutSession("session-1", 1, java.util.concurrent.TimeUnit.SECONDS)) {
            assertNotNull(lease);
            assertSame(mockSession1, lease.getSession());
        }

        assertTrue(registry.sessionExists("session-1"));
        assertTrue(registry.sessionExists("session-2"));
    }

    @Test
    public void testCheckOutSession_ThrowsExceptionForNullSessionId() {
        // Test checking out with null session ID throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            registry.checkOutSession(null, 5, java.util.concurrent.TimeUnit.SECONDS);
        });
    }

    @Test
    public void testSessionLease_ReleasesLockOnClose() throws Exception {
        // Test that SessionLease releases lock properly
        registry.registerSession("test-session", mockSession1);

        SessionLease lease = registry.checkOutSession("test-session", 5, java.util.concurrent.TimeUnit.SECONDS);
        assertFalse(lease.isReleased());
        
        lease.close();
        assertTrue(lease.isReleased());
        
        // Should be able to check out again after release
        try (SessionLease lease2 = 
                registry.checkOutSession("test-session", 1, java.util.concurrent.TimeUnit.SECONDS)) {
            assertNotNull(lease2);
        }
    }

    @Test
    public void testSessionLease_ThrowsExceptionWhenAccessedAfterRelease() throws Exception {
        // Test that accessing session after release throws exception
        registry.registerSession("test-session", mockSession1);

        SessionLease lease = registry.checkOutSession("test-session", 5, java.util.concurrent.TimeUnit.SECONDS);
        lease.close();
        
        assertThrows(IllegalStateException.class, () -> {
            lease.getSession();
        });
    }
}
