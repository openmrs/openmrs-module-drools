package org.openmrs.module.drools.api.impl;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.drools.DroolsConfig;
import org.openmrs.module.drools.KieContainerBuilder;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.event.DroolsEventsManager;
import org.openmrs.module.drools.session.DroolsSessionException;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.openmrs.module.drools.session.SessionPool;
import org.openmrs.module.drools.session.StatefulSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DroolsEngineServiceImpl extends BaseOpenmrsService implements DroolsEngineService {

	@Autowired
	private StatefulSessionRegistry sessionRegistry;

	private KieContainer kieContainer;

	@Autowired
	private KieContainerBuilder kieContainerBuilder;

	@Autowired
	private DroolsConfig droolsConfig;

	private Map<String, RuleSessionConfig> ruleConfigs;

	private SessionPool sessionPool = new SessionPool();

	private DroolsEventsManager eventsManager = new DroolsEventsManager();

	@Override
	public KieSession requestSession(String sessionId) {
		KieSession session;
		if (ruleConfigs == null) {
			ruleConfigs = initializeSessionConfigs();
		}
		if (kieContainer == null) {
			kieContainer = kieContainerBuilder.build();
		}
		if (ruleConfigs.get(sessionId) != null) {
			RuleSessionConfig requestedSessionConfig = ruleConfigs.get(sessionId);
			if (requestedSessionConfig.getStateful()) {
				session = sessionRegistry.requestSession(requestedSessionConfig, kieContainer);
			} else {
				session = sessionPool.borrowSession(sessionId, kieContainer);
			}
			if (session != null) {
				eventsManager.subscribeSessionEventListenersIfNecessary(sessionId, session, ruleConfigs);
				return session;
			}
		} else {
			throw new DroolsSessionException("Can't find session configuration for: " + sessionId);
		}
		return null;
	}

	@Override
	public KieSession evaluate(String sessionId, Collection<? extends OpenmrsObject> facts) {
		KieSession currentSession = requestSession(sessionId);
		if (currentSession != null) {
			facts.forEach(currentSession::insert);
			currentSession.fireAllRules();
		} else {
			throw new DroolsSessionException("Could not establish a KIE session of ID: " + sessionId);
		}
		return currentSession;
	}

	@Override
	public <T> List<T> getSessionObjects(KieSession session, Class<T> tClass) {
		if (session == null) {
			throw new IllegalArgumentException("Session cannot be null");
		}
		Collection<?> rawObjects = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				return tClass.isInstance(object);
			}
		});
		List<T> filteredObjects = new ArrayList<>();
		for (Object obj : rawObjects) {
			filteredObjects.add(tClass.cast(obj));
		}
		return filteredObjects;
	}

	@Override
	public <T> List<T> getSessionObjects(KieSession session, Class<T> tClass, Predicate<T> tPredicate) {
		if (session == null) {
			throw new IllegalArgumentException("Session cannot be null");
		}
		Collection<?> rawObjects = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				if (tClass.isInstance(object)) {
					return tPredicate.test(tClass.cast(object));
				}
				return false;
			}
		});
		List<T> filteredObjects = new ArrayList<>();
		for (Object obj : rawObjects) {
			filteredObjects.add(tClass.cast(obj));
		}
		return filteredObjects;
	}

	@Override
	public void registerRuleProvider(RuleProvider ruleProvider) {
		if (ruleConfigs == null) {
			ruleConfigs = initializeSessionConfigs();
		}
		// register resources
		if (ruleProvider.getRuleResources() != null) {
			ruleProvider.getRuleResources().forEach(kieContainerBuilder::addResource);
		}
		// register session configs
		if (ruleProvider.getSessionConfigs() != null) {
			ruleProvider.getSessionConfigs().forEach(ruleSessionConfig -> {
				if (!ruleConfigs.containsKey(ruleSessionConfig.getSessionId())) {
					ruleConfigs.put(ruleSessionConfig.getSessionId(), ruleSessionConfig);
				}
			});
		}

		// register external evaluators
		droolsConfig.registerProviderExternalEvaluators(ruleProvider);
	}

	@Override
	public List<RuleSessionConfig> getSessionsForAutoStart() {
		if (ruleConfigs == null) {
			ruleConfigs = initializeSessionConfigs();
		}
		return ruleConfigs.values().stream().filter(RuleSessionConfig::getAutoStart).collect(Collectors.toList());
	}

	private Map<String, RuleSessionConfig> initializeSessionConfigs() {
		List<RuleProvider> ruleProviders = droolsConfig.getRuleProviders();
		return ruleProviders.stream().map(RuleProvider::getSessionConfigs).flatMap(List::stream)
				.collect(Collectors.toMap(RuleSessionConfig::getSessionId, ruleSessionConfig -> ruleSessionConfig));
	}

	public StatefulSessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public void setSessionRegistry(StatefulSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	public SessionPool getSessionPool() {
		return sessionPool;
	}

	public void setSessionPool(SessionPool sessionPool) {
		this.sessionPool = sessionPool;
	}

	public KieContainer getKieContainer() {
		return kieContainer;
	}

	public void setKieContainer(KieContainer kieContainer) {
		this.kieContainer = kieContainer;
	}

	public DroolsConfig getDroolsConfig() {
		return droolsConfig;
	}

	public void setDroolsConfig(DroolsConfig droolsConfig) {
		this.droolsConfig = droolsConfig;
	}

}
