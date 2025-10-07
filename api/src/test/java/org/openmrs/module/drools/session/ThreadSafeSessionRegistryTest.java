package org.openmrs.module.drools.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.drools.session.impl.ThreadSafeSessionRegistryImpl;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ThreadSafeSessionRegistry implementation.
 * Verifies thread-safe session management functionality.
 */
public class ThreadSafeSessionRegistryTest {

    private ThreadSafeSessionRegistry registry;

    @Mock
    private KieSession mockSession1;

    @Mock
    private KieSession mockSession2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registry = new ThreadSafeSessionRegistryImpl();
    }

    @Test
    public void testRegisterSession_Success() {
        // Test successful registration
        boolean result = registry.registerSession("test-session-1", mockSession1, true);

        assertTrue(result);
        assertTrue(registry.sessionExists("test-session-1"));
        assertEquals(1, registry.getActiveSessionCount());
    }

    @Test
    public void testRegisterSession_ReturnsFalseForNullSessionId() {
        // Test null session ID returns false
        boolean result = registry.registerSession(null, mockSession1, true);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForEmptySessionId() {
        // Test empty session ID returns false
        boolean result = registry.registerSession("", mockSession1, true);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForNullSession() {
        // Test null KieSession returns false
        boolean result = registry.registerSession("test-session", null, true);
        assertFalse(result);
    }

    @Test
    public void testRegisterSession_ReturnsFalseForDuplicateSessionId() {
        // Test duplicate session ID returns false
        boolean first = registry.registerSession("test-session", mockSession1, true);
        boolean second = registry.registerSession("test-session", mockSession2, true);
        
        assertTrue(first);
        assertFalse(second);
    }

    @Test
    public void testGetSession_ReturnsSessionWhenExists() {
        // Test retrieving existing session
        registry.registerSession("test-session", mockSession1, true);

        Optional<KieSession> result = registry.getSession("test-session");

        assertTrue(result.isPresent());
        assertSame(mockSession1, result.get());
    }

    @Test
    public void testGetSession_ReturnsEmptyWhenNotExists() {
        // Test retrieving non-existent session
        Optional<KieSession> result = registry.getSession("non-existent");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetSession_ReturnsEmptyForNullSessionId() {
        // Test null session ID returns empty
        Optional<KieSession> result = registry.getSession(null);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetSession_UpdatesAccessMetadata() {
        // Test that getting session updates metadata
        registry.registerSession("test-session", mockSession1, true);

        SessionMetadata metadataBefore = registry.getSessionMetadata("test-session");
        int accessCountBefore = metadataBefore.getAccessCount();

        registry.getSession("test-session");

        SessionMetadata metadataAfter = registry.getSessionMetadata("test-session");
        int accessCountAfter = metadataAfter.getAccessCount();

        assertEquals(accessCountBefore + 1, accessCountAfter);
    }

    @Test
    public void testRemoveSession_Success() {
        // Test successful session removal
        registry.registerSession("test-session", mockSession1, true);

        assertTrue(registry.removeSession("test-session"));
        assertFalse(registry.sessionExists("test-session"));
        assertEquals(0, registry.getActiveSessionCount());
        verify(mockSession1, times(1)).dispose();
    }

    @Test
    public void testRemoveSession_ReturnsFalseWhenNotExists() {
        // Test removing non-existent session
        assertFalse(registry.removeSession("non-existent"));
    }

    @Test
    public void testRemoveSession_ReturnsFalseForNullSessionId() {
        // Test null session ID returns false
        assertFalse(registry.removeSession(null));
    }

    @Test
    public void testSessionExists_ReturnsTrueWhenExists() {
        // Test session existence check
        registry.registerSession("test-session", mockSession1, true);

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
    public void testGetActiveSessionIds_ReturnsAllSessions() {
        // Test getting all active session IDs
        registry.registerSession("session-1", mockSession1, true);
        registry.registerSession("session-2", mockSession2, false);

        Collection<String> activeIds = registry.getActiveSessionIds();

        assertEquals(2, activeIds.size());
        assertTrue(activeIds.contains("session-1"));
        assertTrue(activeIds.contains("session-2"));
    }

    @Test
    public void testGetAutoStartableSessionIds_ReturnsOnlyAutoStartable() {
        // Test getting only auto-startable session IDs
        registry.registerSession("auto-session", mockSession1, true);
        registry.registerSession("manual-session", mockSession2, false);

        Collection<String> autoStartableIds = registry.getAutoStartableSessionIds();

        assertEquals(1, autoStartableIds.size());
        assertTrue(autoStartableIds.contains("auto-session"));
        assertFalse(autoStartableIds.contains("manual-session"));
    }

    @Test
    public void testGetSessionMetadata_ReturnsMetadataWhenExists() {
        // Test retrieving session metadata
        registry.registerSession("test-session", mockSession1, true);

        SessionMetadata metadata = registry.getSessionMetadata("test-session");

        assertNotNull(metadata);
        assertEquals("test-session", metadata.getSessionId());
        assertTrue(metadata.isAutoStartable());
        assertNotNull(metadata.getCreatedAt());
        assertNotNull(metadata.getCreatedByThread());
        assertEquals(0, metadata.getAccessCount());
    }

    @Test
    public void testGetSessionMetadata_ReturnsNullWhenNotExists() {
        // Test metadata for non-existent session
        SessionMetadata metadata = registry.getSessionMetadata("non-existent");

        assertNull(metadata);
    }

    @Test
    public void testGetSessionMetadata_ReturnsNullForNullSessionId() {
        // Test null session ID
        SessionMetadata metadata = registry.getSessionMetadata(null);

        assertNull(metadata);
    }

    @Test
    public void testGetActiveSessionCount_ReturnsCorrectCount() {
        // Test active session count
        assertEquals(0, registry.getActiveSessionCount());

        registry.registerSession("session-1", mockSession1, true);
        assertEquals(1, registry.getActiveSessionCount());

        registry.registerSession("session-2", mockSession2, true);
        assertEquals(2, registry.getActiveSessionCount());

        registry.removeSession("session-1");
        assertEquals(1, registry.getActiveSessionCount());

        registry.removeSession("session-2");
        assertEquals(0, registry.getActiveSessionCount());
    }

    @Test
    public void testCleanupExpiredSessions_DoesNotRemoveRecentSessions() {
        // Test that recent sessions are not cleaned up
        registry.registerSession("recent-session", mockSession1, true);

        registry.cleanupExpiredSessions();

        assertTrue(registry.sessionExists("recent-session"));
    }

    @Test
    public void testMultipleOperations_MaintainsConsistency() {
        // Test multiple operations maintain consistency
        registry.registerSession("session-1", mockSession1, true);
        registry.registerSession("session-2", mockSession2, false);

        assertEquals(2, registry.getActiveSessionCount());
        assertEquals(2, registry.getActiveSessionIds().size());
        assertEquals(1, registry.getAutoStartableSessionIds().size());

        Optional<KieSession> session1 = registry.getSession("session-1");
        assertTrue(session1.isPresent());

        SessionMetadata metadata1 = registry.getSessionMetadata("session-1");
        assertEquals(1, metadata1.getAccessCount());

        registry.removeSession("session-1");

        assertEquals(1, registry.getActiveSessionCount());
        assertFalse(registry.sessionExists("session-1"));
        assertTrue(registry.sessionExists("session-2"));
    }

    @Test
    public void testCheckOutSession_Success() throws Exception {
        // Test successful session check-out
        registry.registerSession("test-session", mockSession1, true);

        try (org.openmrs.module.drools.session.SessionLease lease = 
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
        registry.registerSession("test-session", mockSession1, true);

        // Use a CountDownLatch to coordinate threads
        final java.util.concurrent.CountDownLatch latch1 = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch latch2 = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicBoolean thread2TimedOut = new java.util.concurrent.atomic.AtomicBoolean(false);

        // Thread 1: Acquire lock and hold it
        Thread thread1 = new Thread(() -> {
            try (org.openmrs.module.drools.session.SessionLease lease = 
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
        try (org.openmrs.module.drools.session.SessionLease lease = 
                registry.checkOutSession("test-session", 1, java.util.concurrent.TimeUnit.SECONDS)) {
            assertNotNull(lease);
        }
    }
}

