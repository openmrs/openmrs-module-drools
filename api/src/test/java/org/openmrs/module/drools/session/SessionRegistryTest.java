package org.openmrs.module.drools.session;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.impl.SessionRegistryImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SessionRegistry} implementation.
 */
public class SessionRegistryTest {

    private SessionRegistry registry;

    @Before
    public void setUp() {
        registry = new SessionRegistryImpl();
    }

    @Test
    public void testRegisterSession() {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        assertTrue("Session should exist after registration", registry.sessionExists("test-session"));
    }

    @Test
    public void testCheckOutSession() throws InterruptedException {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        try (SessionLease lease = registry.checkOutSession("test-session", 5, TimeUnit.SECONDS)) {
            assertNotNull("Lease should not be null", lease);
            assertNotNull("Session from lease should not be null", lease.getSession());
            assertEquals("Session from lease should match registered session", mockSession, lease.getSession());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckOutNonExistentSession() throws InterruptedException {
        registry.checkOutSession("non-existent", 1, TimeUnit.SECONDS);
    }

    @Test
    public void testSessionExists() {
        assertFalse("Session should not exist before registration", registry.sessionExists("test-session"));

        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        assertTrue("Session should exist after registration", registry.sessionExists("test-session"));
    }

    @Test
    public void testSessionExistsWithNullId() {
        assertFalse("Null session ID should return false", registry.sessionExists(null));
    }

    @Test
    public void testSessionExistsWithEmptyId() {
        assertFalse("Empty session ID should return false", registry.sessionExists(""));
    }

    @Test
    public void testRemoveSession() {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        assertTrue("Session should exist before removal", registry.sessionExists("test-session"));

        registry.removeSession("test-session");

        assertFalse("Session should not exist after removal", registry.sessionExists("test-session"));
    }

    @Test
    public void testRemoveNonExistentSession() {
        registry.removeSession("non-existent");
    }

    @Test
    public void testConcurrentCheckout() throws InterruptedException {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable task = () -> {
            try (SessionLease lease = registry.checkOutSession("test-session", 100, TimeUnit.MILLISECONDS)) {
                successCount.incrementAndGet();
                Thread.sleep(200);
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);

        thread1.start();
        thread2.start();

        latch.await(2, TimeUnit.SECONDS);

        assertEquals("Exactly one thread should succeed", 1, successCount.get());
        assertEquals("Exactly one thread should fail", 1, failureCount.get());
    }

    @Test
    public void testMultipleSessionsConcurrent() throws InterruptedException {
        KieSession mockSession1 = mock(KieSession.class);
        KieSession mockSession2 = mock(KieSession.class);

        registry.registerSession("session-1", mockSession1);
        registry.registerSession("session-2", mockSession2);

        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable task1 = () -> {
            try (SessionLease lease = registry.checkOutSession("session-1", 1, TimeUnit.SECONDS)) {
                successCount.incrementAndGet();
                Thread.sleep(100);
            } catch (Exception e) {
                // Should not happen
            } finally {
                latch.countDown();
            }
        };

        Runnable task2 = () -> {
            try (SessionLease lease = registry.checkOutSession("session-2", 1, TimeUnit.SECONDS)) {
                successCount.incrementAndGet();
                Thread.sleep(100);
            } catch (Exception e) {
                // Should not happen
            } finally {
                latch.countDown();
            }
        };

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        latch.await(2, TimeUnit.SECONDS);

        assertEquals("Both threads should succeed", 2, successCount.get());
    }

    @Test
    public void testSessionLeaseAutoClose() throws InterruptedException {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("test-session", mockSession);

        try (SessionLease lease1 = registry.checkOutSession("test-session", 1, TimeUnit.SECONDS)) {
            assertNotNull(lease1.getSession());
        }

        try (SessionLease lease2 = registry.checkOutSession("test-session", 1, TimeUnit.SECONDS)) {
            assertNotNull(lease2.getSession());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterSessionWithNullId() {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession(null, mockSession);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterSessionWithEmptyId() {
        KieSession mockSession = mock(KieSession.class);
        registry.registerSession("", mockSession);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterSessionWithNullSession() {
        registry.registerSession("test-session", null);
    }
}
