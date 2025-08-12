/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.drools;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.ResourceType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.ExternalEvaluatorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains module's config.
 */
@Configuration
public class DroolsConfig {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private List<RuleProvider> ruleProviders;

	@Autowired
	private ExternalEvaluatorManager externalEvaluatorManager;

	private final String GLOBALS_DRL_PATH = "rules/globals.drl";

	@Bean
	public KieContainerBuilder kieContainerBuilder() {
		// Apache POI (used by Drools decision tables) relies on the Xerces XML parser from the JDK.
		// However, OpenMRS core pulls in a third party Xerces variant (xercesImpl) which has different parser
		// configuration classes. When both are on the classpath, the JVM may pick the wrong one,
		// causing XML parsing errors at runtime. An alternative solution could be using an uber-jar.
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		ruleProviders = Context.getRegisteredComponents(RuleProvider.class).stream().filter(RuleProvider::isEnabled)
				.collect(Collectors.toList());

		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

		kieFileSystem.write(kieServices.getResources().newClassPathResource(GLOBALS_DRL_PATH)
				.setResourceType(ResourceType.DRL));

		KieContainerBuilder builder = new KieContainerBuilder(kieServices, kieFileSystem);

		// Load rules
		for (RuleProvider provider : ruleProviders) {
			registerProviderExternalEvaluators(provider);
			provider.getRuleResources().forEach(builder::addResource);
		}

		return builder;
	}

	public void registerProviderExternalEvaluators(RuleProvider provider) {
		Map<String, ExternalEvaluator> evaluatorMap = provider.getExternalEvaluators();
		if (evaluatorMap != null) {
			for (Map.Entry<String, ExternalEvaluator> entry : evaluatorMap.entrySet()) {
				if (externalEvaluatorManager.supportsEvaluatorWithId(entry.getKey())) {
					log.warn("External evaluator with id " + entry.getKey() + " already exists. Ignoring.");
				} else {
					log.debug("Adding external evaluator with id " + entry.getKey());
					externalEvaluatorManager.addEvaluator(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public List<RuleProvider> getRuleProviders() {
		return ruleProviders;
	}

	public void setRuleProviders(List<RuleProvider> ruleProviders) {
		this.ruleProviders = ruleProviders;
	}

	public ExternalEvaluatorManager getExternalEvaluatorManager() {
		return externalEvaluatorManager;
	}

	public void setExternalEvaluatorManager(ExternalEvaluatorManager externalEvaluatorManager) {
		this.externalEvaluatorManager = externalEvaluatorManager;
	}
}
