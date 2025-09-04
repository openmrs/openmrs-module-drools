package org.openmrs.module.drools.api;

import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.DroolsSessionConfig;

import java.util.List;
import java.util.Map;

public interface RuleProvider {

	Boolean isEnabled();

	List<RuleResource> getRuleResources();

	List<DroolsSessionConfig> getSessionConfigs();

	Map<String, ExternalEvaluator> getExternalEvaluators();
}
