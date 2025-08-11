package org.openmrs.module.drools.api.impl;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.AgendaFilter;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.drools.DroolsConfig;
import org.openmrs.module.drools.KieContainerBuilder;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.event.DroolsEventsManager;
import org.openmrs.module.drools.session.AgendaFilterByNameOrGroup;
import org.openmrs.module.drools.session.DroolsSessionException;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.openmrs.module.drools.session.StatefulSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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

	private DroolsEventsManager eventsManager = new DroolsEventsManager();

	private final Map<String, Map <String, Object>> globalBindings = new HashMap<>();

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
			session = sessionRegistry.requestSession(requestedSessionConfig, kieContainer, globalBindings);
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
			String allowedAgendaGroup = ruleConfigs.get(sessionId).getAgendaGroup();
			AgendaFilter agendaFilter = ruleConfigs.get(sessionId).getAgendaFilter();
			if (StringUtils.isNotBlank(allowedAgendaGroup)) {
				currentSession.getAgenda().getAgendaGroup(allowedAgendaGroup).setFocus();
				if (agendaFilter == null) {
					agendaFilter = new AgendaFilterByNameOrGroup(null, allowedAgendaGroup);
				}
			}
			currentSession.fireAllRules(agendaFilter);
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
				if (!globalBindings.containsKey(ruleSessionConfig.getSessionId())) {
					globalBindings.put(ruleSessionConfig.getSessionId(), ruleSessionConfig.getGlobals());
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
		Map<String, RuleSessionConfig> sessionConfigMap = ruleProviders.stream().map(RuleProvider::getSessionConfigs).flatMap(List::stream)
				.collect(Collectors.toMap(RuleSessionConfig::getSessionId, ruleSessionConfig -> ruleSessionConfig));
		sessionConfigMap.forEach((sessionId, config) -> {
			globalBindings.put(sessionId, config.getGlobals());
		});
		return sessionConfigMap;
	}

	public StatefulSessionRegistry getSessionRegistry() {
		return sessionRegistry;
	}

	public void setSessionRegistry(StatefulSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
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
