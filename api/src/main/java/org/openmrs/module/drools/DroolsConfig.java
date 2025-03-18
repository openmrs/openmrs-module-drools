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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.ExternalEvaluatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Contains module's config.
 */
@Configuration
public class DroolsConfig {

	Log log = LogFactory.getLog(DroolsConfig.class);

	private List<RuleProvider> ruleProviders;

	@Autowired
	private ExternalEvaluatorManager externalEvaluatorManager;

	private final String GLOBALS_DRL_PATH = "rules/globals.drl";

	@Bean
	public KieContainer kieContainer() {
		ruleProviders = Context.getRegisteredComponents(RuleProvider.class);
		if (ruleProviders.isEmpty()) {
			throw new IllegalStateException("No defined RuleProviders");
		}
		KieServices kieServices = KieServices.Factory.get();
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

		kieFileSystem.write(kieServices.getResources().newClassPathResource(GLOBALS_DRL_PATH)
				.setResourceType(ResourceType.DRL));

		// Load rules
		for (RuleProvider provider : ruleProviders) {
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
			for (RuleResource resource : provider.getRuleResources()) {
				kieFileSystem.write(kieServices.getResources().newClassPathResource(resource.getPath())
						.setResourceType(resource.getResourceType()));
			}
		}

		// Build the KieContainer
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
		return kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
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
