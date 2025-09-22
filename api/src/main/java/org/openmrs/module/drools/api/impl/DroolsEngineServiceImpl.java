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
import org.openmrs.module.drools.session.*;
import org.openmrs.module.drools.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DroolsEngineServiceImpl extends BaseOpenmrsService implements DroolsEngineService {

	private KieContainer kieContainer;

	@Autowired
	private KieContainerBuilder kieContainerBuilder;

	@Autowired
	private DroolsConfig droolsConfig;

	private Map<String, DroolsSessionConfig> ruleConfigs;

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
			session = CommonUtils.createKieSession(kieContainer, ruleConfigs.get(sessionId), droolsConfig.getExternalEvaluatorManager(), globalBindings);
			eventsManager.subscribeSessionEventListenersIfNecessary(sessionId, session, ruleConfigs);

			return session;
		} else {
			throw new DroolsSessionException("Can't find session configuration for: " + sessionId);
		}
	}

	@Override
	public KieSession evaluate(String sessionId, Collection<? extends OpenmrsObject> facts) {
		KieSession currentSession = requestSession(sessionId);
		if (currentSession != null) {
			facts.forEach(currentSession::insert);
			currentSession.fireAllRules(getSessionAgendaFilter(currentSession, ruleConfigs.get(sessionId)));
		} else {
			throw new DroolsSessionException("Could not establish a KIE session of ID: " + sessionId);
		}
		return currentSession;
	}

	@Override
	public DroolsExecutionResult evaluate(String sessionId, Collection<Object> facts, Class<?> resultClazz) {
		KieSession currentSession = requestSession(sessionId);
		DroolsExecutionResult result;
		if (currentSession != null) {
			facts.forEach(currentSession::insert);
			int fired = currentSession.fireAllRules(getSessionAgendaFilter(currentSession, ruleConfigs.get(sessionId)));
			List<?> results = getSessionObjects(currentSession, resultClazz);
			result = new DroolsExecutionResult(sessionId, fired, (List<Object>) results);
			currentSession.dispose();

		} else {
			throw new DroolsSessionException("Could not establish a KIE session of ID: " + sessionId);
		}

		return result;
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
		if (!ruleProvider.isEnabled()) {
			return;
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
	public List<DroolsSessionConfig> getSessionsForAutoStart() {
		if (ruleConfigs == null) {
			ruleConfigs = initializeSessionConfigs();
		}
		return ruleConfigs.values().stream().filter(DroolsSessionConfig::getAutoStart).collect(Collectors.toList());
	}

	@Override
	public DroolsSessionConfig getSessionConfig(String sessionId) {
		if (ruleConfigs == null) {
			ruleConfigs = initializeSessionConfigs();
		}
		return ruleConfigs.get(sessionId);
	}

	private Map<String, DroolsSessionConfig> initializeSessionConfigs() {
		List<RuleProvider> ruleProviders = droolsConfig.getRuleProviders();
		Map<String, DroolsSessionConfig> sessionConfigMap = ruleProviders.stream().map(RuleProvider::getSessionConfigs).flatMap(List::stream)
				.collect(Collectors.toMap(DroolsSessionConfig::getSessionId, ruleSessionConfig -> ruleSessionConfig));
		sessionConfigMap.forEach((sessionId, config) -> {
			globalBindings.put(sessionId, config.getGlobals());
		});
		return sessionConfigMap;
	}

	private AgendaFilter getSessionAgendaFilter(KieSession session,  DroolsSessionConfig sessionConfig) {
		String allowedAgendaGroup = sessionConfig.getAgendaGroup();
		AgendaFilter agendaFilter = sessionConfig.getAgendaFilter();
		if (StringUtils.isNotBlank(allowedAgendaGroup)) {
			session.getAgenda().getAgendaGroup(allowedAgendaGroup).setFocus();
			if (agendaFilter == null) {
				return new AgendaFilterByNameOrGroup(null, allowedAgendaGroup);
			}
		}
		return agendaFilter;
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
