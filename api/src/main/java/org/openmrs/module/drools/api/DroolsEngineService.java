package org.openmrs.module.drools.api;

import org.kie.api.runtime.KieSession;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.drools.session.DroolsSessionException;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.SessionLease;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public interface DroolsEngineService extends OpenmrsService {

	/**
	 * Evaluates a collection of facts in a newly created Drools session.
	 * <p>
	 * Note: The caller is responsible for disposing of the session after use
	 * to prevent memory leaks.
	 *
	 * @param sessionId the identifier of a preconfigured Drools session to use
	 * @param facts     the collection of OpenMRS objects to insert into the session as facts
	 * @return the Drools {@link KieSession} after rule evaluation
	 * @throws DroolsSessionException if the session could not be established,
	 *                                or if the configuration is missing or invalid
	 */
	public KieSession evaluate(String sessionId, Collection<? extends OpenmrsObject> facts);

	/**
	 * Evaluates a collection of facts in a new Drools session and collects results.
	 * <p>
	 * Unlike {@link #evaluate(String, Collection)}, this method disposes of the session
	 * automatically after execution and returns the results of evaluation.
	 *
	 * @param sessionId   the identifier of a preconfigured Drools session to use
	 * @param facts       the collection of objects to evaluate as facts
	 * @param resultClassName the fully qualified class name of objects to extract from the session.
	 * @return a {@link DroolsExecutionResult} containing all matching objects of type {@code resultClazz}
	 * @throws DroolsSessionException if the session could not be established,
	 *                                or if the configuration is missing or invalid
	 */
	public DroolsExecutionResult evaluate(String sessionId, Collection<Object> facts, String resultClassName);

	/**
	 * Retrieves a collection of facts of the specified type from a Drools session.
	 * 
	 * @param <T>     the type of objects to retrieve
	 * @param session the Drools session to query
	 * @param tClass  the class object representing the type to retrieve
	 * @return a list of objects of the specified type from the session
	 * @throws DroolsSessionException if the session does not exist or cannot be
	 *                                accessed
	 */
	public <T> List<T> getSessionObjects(KieSession session, Class<T> tClass);

	/**
	 * Retrieves facts from a Drools session that are of the specified type and
	 * satisfy the given predicate.
	 * 
	 * @param <T>        the type of objects to retrieve
	 * @param session    the Drools session to query
	 * @param tClass     the class object representing the type to retrieve
	 * @param tPredicate the predicate that returned objects must satisfy
	 * @return a list of matching objects from the session
	 * @throws DroolsSessionException if the session does not exist or cannot be
	 *                                accessed
	 */
	public <T> List<T> getSessionObjects(KieSession session, Class<T> tClass, Predicate<T> tPredicate);

	/**
	 * Creates and initializes a new Drools session using a predefined configuration.
	 * <p>
	 * The provided {@code sessionId} must correspond to an existing session configuration.
	 *
	 * @param sessionId the identifier of the session to create
	 * @return the newly created {@link KieSession}
	 * @throws DroolsSessionException if no matching session configuration exists
	 *                                or if initialization fails
	 */
	public KieSession requestSession(String sessionId);

	/**
	 * Manually registers a rule provider and its associated resources into the
	 * Drools container.
	 * 
	 * @param ruleProvider the provider to be registered
	 */
	public void registerRuleProvider(RuleProvider ruleProvider);

	/**
	 * Returns all registered session configurations configured for auto-start.
	 */
	public List<DroolsSessionConfig> getSessionsForAutoStart();

	public DroolsSessionConfig getSessionConfig(String sessionId);

	/**
	 * Registers an auto-start session in the session registry.
	 * <p>
	 * This method should be called during module startup for sessions configured
	 * with autoStart=true. Registered sessions can be reused across multiple
	 * requests via {@link #checkOutAutoStartSession}.
	 * 
	 * @param sessionId the unique identifier for the session
	 * @param session   the KieSession instance to register
	 */
	public void registerAutoStartSession(String sessionId, KieSession session);

	/**
	 * Checks out an auto-start session for exclusive use.
	 * <p>
	 * This method provides thread-safe access to a registered auto-start session.
	 * The returned {@link SessionLease} must be used in a try-with-resources block
	 * to ensure proper lock release.
	 * <p>
	 * Example usage:
	 * 
	 * <pre>
	 * try (SessionLease lease = service.checkOutAutoStartSession("mySession", 5, TimeUnit.SECONDS)) {
	 * 	KieSession session = lease.getSession();
	 * 	// Use session safely
	 * } // Lock automatically released
	 * </pre>
	 * 
	 * @param sessionId the unique identifier of the session to check out
	 * @param timeout   the maximum time to wait for the lock
	 * @param unit      the time unit of the timeout argument
	 * @return a SessionLease providing exclusive access to the session
	 * @throws IllegalArgumentException if the session does not exist
	 * @throws RuntimeException         if the lock cannot be acquired within the
	 *                                  timeout
	 * @throws InterruptedException     if the thread is interrupted while waiting
	 *                                  for the lock
	 */
	public SessionLease checkOutAutoStartSession(String sessionId, long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * Checks if a session is registered in the session registry.
	 * 
	 * @param sessionId the unique identifier of the session
	 * @return true if the session is registered, false otherwise
	 */
	public boolean isSessionRegistered(String sessionId);

}
