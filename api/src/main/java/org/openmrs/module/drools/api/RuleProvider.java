package org.openmrs.module.drools.api;

import org.openmrs.module.drools.RuleResource;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.RuleSessionConfig;

import java.util.List;
import java.util.Map;

public interface RuleProvider {
	
	List<RuleResource> getRuleResources();
	
	List<RuleSessionConfig> getSessionConfigs();
	
	Map<String, ExternalEvaluator> getExternalEvaluators();
}
