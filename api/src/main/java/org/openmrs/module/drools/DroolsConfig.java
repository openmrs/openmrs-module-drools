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

import org.drools.compiler.kie.builder.impl.KieServicesImpl;
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
		log.info("=== DROOLS: Creating KieContainerBuilder bean (rule providers loaded lazily) ===");

		// Apache POI (used by Drools decision tables) relies on the Xerces XML parser from the JDK.
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

		// Create KieServices and KieFileSystem
		KieServices kieServices = new KieServicesImpl();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

		// Load globals.drl
		kieFileSystem.write(kieServices.getResources().newClassPathResource(GLOBALS_DRL_PATH)
				.setResourceType(ResourceType.DRL));

		// Create builder - rule providers will be loaded lazily when first session is requested
		KieContainerBuilder builder = new KieContainerBuilder(kieServices, kieFileSystem);

		log.info("=== DROOLS: KieContainerBuilder bean created successfully ===");
		return builder;
	}

	/**
	 * Loads rule providers from OpenMRS Context. Called lazily when needed.
	 */
	private void loadRuleProvidersIfNeeded() {
		if (ruleProviders != null) {
			return; // Already loaded
		}
		log.info("DROOLS: Loading rule providers from Context");
		List<RuleProvider> allProviders = Context.getRegisteredComponents(RuleProvider.class);
		ruleProviders = new java.util.ArrayList<>();
		if (allProviders != null) {
			for (RuleProvider provider : allProviders) {
				if (provider != null && provider.isEnabled()) {
					ruleProviders.add(provider);
					registerProviderExternalEvaluators(provider);
				}
			}
		}
		log.info("DROOLS: Loaded {} enabled rule providers", ruleProviders.size());
	}

	/**
	 * Adds rule resources from providers to the builder. Called before building KieContainer.
	 */
	public void addRuleResourcesToBuilder(KieContainerBuilder builder) {
		loadRuleProvidersIfNeeded();
		for (RuleProvider provider : ruleProviders) {
			provider.getRuleResources().forEach(builder::addResource);
		}
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
		loadRuleProvidersIfNeeded();
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
