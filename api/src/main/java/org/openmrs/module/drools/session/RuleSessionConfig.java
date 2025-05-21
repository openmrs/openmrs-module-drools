package org.openmrs.module.drools.session;

import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.rule.AgendaFilter;
import org.openmrs.module.drools.event.DroolsSystemEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleSessionConfig {

	private String sessionId;

	/**
	 * Stateless sessions will be disposed.
	 */
	private Boolean isStateful;

	/**
	 * If true, the session will be bootstrapped automatically when the module is
	 * started.
	 *
	 * Note: Auto started sessions will be disposed when the module stops while on fly sessions will be disposed when the thread ends (finally)
	 */
	private Boolean autoStart;

	private HashMap<String, Object> globals;

	private int initialPoolSize;

	/**
	 * Specifies the agenda group for this session. Agenda groups allow for partitioning rules
	 * within a single KieBase and controlling their execution in specific sessions.
	 *<br/>
	 * Notes: Currently, all resources are stored in the default KieBase, which means all registered sessions
	 * have access to and can potentially trigger all available rules. Using agenda groups provides a way
	 * to logically organize rules and control which subset of rules can be activated within a specific session.
	 */
	private String agendaGroup;

	private AgendaFilter agendaFilter;

	private List<RuleRuntimeEventListener> sessionRuntimeEventListeners;

	private List<DroolsSystemEventListener> systemEventListeners;

	public RuleSessionConfig() {
		this.sessionRuntimeEventListeners = new ArrayList<>();
		this.globals = new HashMap<>();
	}

	public RuleSessionConfig(String sessionId, Boolean isStateful, Boolean autoStart, HashMap<String, Object> globals,
			List<RuleRuntimeEventListener> sessionRuntimeEventListeners,
			List<DroolsSystemEventListener> systemEventListeners) {
		this.sessionId = sessionId;
		this.isStateful = isStateful;
		this.autoStart = autoStart;
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

	public Boolean getAutoStart() {
		return autoStart;
	}

	public void setAutoStart(Boolean autoStart) {
		this.autoStart = autoStart;
	}

	public String getAgendaGroup() {
		return agendaGroup;
	}

	public void setAgendaGroup(String agendaGroup) {
		this.agendaGroup = agendaGroup;
	}

	public AgendaFilter getAgendaFilter() {
		return agendaFilter;
	}

	public void setAgendaFilter(AgendaFilter agendaFilter) {
		this.agendaFilter = agendaFilter;
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
