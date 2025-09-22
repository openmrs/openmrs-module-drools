package org.openmrs.module.drools.session;

import java.util.Map;

// TODO: This component appears to be vestigial and may no longer be needed.
//       Review its utility and remove if we are not supporting external evaluators
public interface ExternalEvaluator {
	
	public void evaluate(Map<String, Object> context);
}
