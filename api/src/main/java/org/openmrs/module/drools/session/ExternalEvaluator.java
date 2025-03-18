package org.openmrs.module.drools.session;

import java.util.Map;

public interface ExternalEvaluator {
	
	public void evaluate(Map<String, Object> context);
}
