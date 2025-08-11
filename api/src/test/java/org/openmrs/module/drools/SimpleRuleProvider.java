package org.openmrs.module.drools;

import org.kie.api.io.ResourceType;
import org.openmrs.module.drools.api.RuleProvider;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.drools.session.ExternalEvaluator;
import org.openmrs.module.drools.session.RuleSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("testRuleProvider")
public class SimpleRuleProvider implements RuleProvider {

	private static final String DECISION_TABLE_PATH = "decision_tables/bp_rules.drl.xlsx";

	@Autowired
	private DroolsCalculationService calculationService;

	public Boolean isEnabled() {
		return true;
	}

	@Override
	public List<RuleResource> getRuleResources() {
		return Collections
				.singletonList(new RuleResource("BP rules", DECISION_TABLE_PATH, ResourceType.DTABLE));
	}

	@Override
	public List<RuleSessionConfig> getSessionConfigs() {
		HashMap<String, Object> globals = new HashMap<>();
		globals.put("calcService", calculationService);
		return Collections.singletonList(new RuleSessionConfig("test1", true, globals, Collections.emptyList(),
				Collections.emptyList()));
	}

	@Override
	public Map<String, ExternalEvaluator> getExternalEvaluators() {
		return null;
	}

}
