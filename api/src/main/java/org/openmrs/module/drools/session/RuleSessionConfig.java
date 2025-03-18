package org.openmrs.module.drools.session;

import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.openmrs.module.drools.event.DroolsSystemEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleSessionConfig {

	private String sessionId;

	private Boolean isStateful;

	private HashMap<String, Object> globals;

	private int initialPoolSize;

	private List<RuleRuntimeEventListener> sessionRuntimeEventListeners;

	private List<DroolsSystemEventListener> systemEventListeners;

	public RuleSessionConfig() {
		this.sessionRuntimeEventListeners = new ArrayList<>();
		this.globals = new HashMap<>();
	}

	public RuleSessionConfig(String sessionId, Boolean isStateful, HashMap<String, Object> globals,
			List<RuleRuntimeEventListener> sessionRuntimeEventListeners,
			List<DroolsSystemEventListener> systemEventListeners) {
		this.sessionId = sessionId;
		this.isStateful = isStateful;
		this.globals = globals;
		this.sessionRuntimeEventListeners = sessionRuntimeEventListeners;
		this.systemEventListeners = systemEventListeners;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Boolean getStateful() {
		return isStateful;
	}

	public void setStateful(Boolean stateful) {
		isStateful = stateful;
	}

	public HashMap<String, Object> getGlobals() {
		return globals;
	}

	public void setGlobals(HashMap<String, Object> globals) {
		this.globals = globals;
	}

	public int getInitialPoolSize() {
		return this.initialPoolSize;
	}

	public void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	public List<RuleRuntimeEventListener> getSessionRuntimeEventListeners() {
		return sessionRuntimeEventListeners;
	}

	public void setSessionRuntimeEventListeners(List<RuleRuntimeEventListener> sessionRuntimeEventListeners) {
		this.sessionRuntimeEventListeners = sessionRuntimeEventListeners;
	}

	public List<DroolsSystemEventListener> getSystemEventListeners() {
		return systemEventListeners;
	}

	public void setSystemEventListeners(List<DroolsSystemEventListener> systemEventListeners) {
		this.systemEventListeners = systemEventListeners;
	}
}
