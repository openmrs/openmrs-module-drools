package org.openmrs.module.drools.api;

import org.kie.api.runtime.KieSession;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.drools.session.DroolsSessionException;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface DroolsEngineService extends OpenmrsService {

	/**
	 * Evaluates a collection of facts in a Drools session.
	 * 
	 * If the session with the given ID does not exist, a new session is created.
	 * 
	 * @param sessionId the identifier of a preconfigured Drools session to use
	 * @param facts     the collection of OpenMRS objects to evaluate as facts
	 * @return KieSession the Drools session after evaluation
	 * @throws DroolsSessionException if the session could not be established or the
	 *                                session configuration is missing or invalid
	 */
	public KieSession evaluate(String sessionId, Collection<? extends OpenmrsObject> facts);

	/**
	 * Retrieves a collection of facts of the specified type from a Drools session.
	 * 
	 * @param <T>     the type of objects to retrieve
	 * @param session the Drools session to query
	 * @param tClass  the class object representing the type to retrieve
	 * @return List<T> a list of objects of the specified type from the session
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
	 * @return List<T> a list of matching objects from the session
	 * @throws DroolsSessionException if the session does not exist or cannot be
	 *                                accessed
	 */
	public <T> List<T> getSessionObjects(KieSession session, Class<T> tClass, Predicate<T> tPredicate);

	/**
	 * Returns existing session or creates a new one if it does not exist.
	 * 
	 * @param sessionId the identifier of the session to request
	 * @return KieSession the requested session
	 */
	public KieSession requestSession(String sessionId);

}
