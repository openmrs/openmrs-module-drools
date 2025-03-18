package org.openmrs.module.drools.session;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExternalEvaluatorManager {
	
	private Map<String, ExternalEvaluator> evaluatorMap;
	
	public ExternalEvaluatorManager() {
        this.evaluatorMap = new HashMap<>();
    }
	
	public ExternalEvaluatorManager(Map<String, ExternalEvaluator> evaluatorMap) {
		this.evaluatorMap = evaluatorMap;
	}
	
	public void evaluate(String evaluatorId, Map<String, Object> context) {
		ExternalEvaluator evaluator = evaluatorMap.get(evaluatorId);
		if (evaluator != null) {
			evaluator.evaluate(context);
		} else {
			throw new IllegalStateException("Unknown External Evaluator: " + evaluatorId);
		}
	}
	
	public Boolean supportsEvaluatorWithId(String evaluatorId) {
		return evaluatorMap.containsKey(evaluatorId);
	}
	
	public void addEvaluator(String evaluatorId, ExternalEvaluator evaluator) {
		this.evaluatorMap.put(evaluatorId, evaluator);
	}
	
	public void disposeEvaluator(String evaluatorId) {
		this.evaluatorMap.remove(evaluatorId);
	}
	
	public Map<String, ExternalEvaluator> getEvaluatorMap() {
		return evaluatorMap;
	}
	
	public void setEvaluatorMap(Map<String, ExternalEvaluator> evaluatorMap) {
		this.evaluatorMap = evaluatorMap;
	}
}
