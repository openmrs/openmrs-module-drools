package org.openmrs.module.drools.patientflags;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.kie.api.runtime.KieSession;

import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.session.AgendaFilterByNameOrGroup;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.FlagValidationResult;
import org.openmrs.module.patientflags.PatientFlag;
import org.openmrs.module.patientflags.evaluator.FlagEvaluator;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DroolsFlagEvaluator implements FlagEvaluator {
    private final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Boolean eval(Flag flag, Patient patient, Map<Object, Object> contextMap) {
        Cohort cohort = new Cohort();
        cohort.addMember(patient.getId());
        // clear stale context
        contextMap.remove(patient.getId());
        Cohort resultCohort = evalCohort(flag, cohort, contextMap);
        return !resultCohort.isEmpty();
    }

    @Override
    public Cohort evalCohort(Flag flag, Cohort cohort, Map<Object, Object> contextMap) {
        Cohort resultCohort = new Cohort();
        if (cohort == null) {
            log.warn("Cohort is null");
            return resultCohort;
        }
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);

        DroolsFlagConfigDescriptor config = extractDroolsFlagConfig(flag);
        KieSession session = droolsEngineService.requestSession(config.getSession());

        cohort.getActiveMemberships().stream().map(CohortMembership::getPatientId).forEach(id -> {
            Patient patient = Context.getPatientService().getPatient(id);
            session.insert(patient);
        });

        if (StringUtils.isNotBlank(config.getAgendaGroup())) {
            session.getAgenda().getAgendaGroup(config.getAgendaGroup()).setFocus();
        }

        session.fireAllRules(new AgendaFilterByNameOrGroup(config.getRules(), config.getAgendaGroup()));

        for (PatientFlag flagged : droolsEngineService.getSessionObjects(session, PatientFlag.class)) {
            resultCohort.addMember(flagged.getPatient().getPatientId());
            // Store message in context map if one was provided
            if (flagged.getMessage() != null && !flagged.getMessage().isEmpty()) {
                if (!contextMap.containsKey(flagged.getPatient().getPatientId())) {
                    contextMap.put(flagged.getPatient().getPatientId(), new ArrayList<String>());
                }

                @SuppressWarnings("unchecked")
                List<String> messages = (List<String>) contextMap.get(flagged.getPatient().getPatientId());
                messages.add(flagged.getMessage());
            }
        }
        // dispose of session
        session.dispose();
        return resultCohort;
    }

    @Override
    public String evalMessage(Flag flag, int patientId) {
        return flag.getMessage();
    }

    @Override
    public FlagValidationResult validate(Flag flag) {
        try {
            evalCohort(flag, new Cohort(), new HashMap<>());
            return new FlagValidationResult(true);
        } catch (Exception e) {
            return new FlagValidationResult(false, e.getLocalizedMessage());
        }
    }

    private DroolsFlagConfigDescriptor extractDroolsFlagConfig(Flag flag) {
        if (flag == null || StringUtils.isBlank(flag.getCriteria())) {
            throw new IllegalArgumentException("Flag or its criteria must not be null or blank.");
        }

        try {
            return new ObjectMapper().readValue(flag.getCriteria(), DroolsFlagConfigDescriptor.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse DroolsFlagConfigDescriptor from flag criteria.", e);
        }
    }

}
