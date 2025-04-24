package org.openmrs.module.drools.patientflags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.calculation.DroolsCalculationService;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.FlagValidationResult;
import org.openmrs.module.patientflags.evaluator.FlagEvaluator;

// TODO: Handle privilege proxies for non admin users
public class DroolsFlagEvaluator implements FlagEvaluator {
    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public Boolean eval(Flag flag, Patient patient, Map<Object, Object> contextMap) {
        Cohort cohort = new Cohort();
        cohort.addMember(patient.getId());
        Cohort resultCohort = evalCohort(flag, cohort, contextMap);
        return !resultCohort.isEmpty();
    }

    @Override
    public Cohort evalCohort(Flag flag, Cohort cohort, Map<Object, Object> contextMap) {
        // Notes: We shall use the Flag object to track the session and the target rules
        // Flag.name will be treated as the name of the rule
        // Flag.criteria will be treated as the session name

        Cohort resultCohort = new Cohort();
        if (cohort == null) {
            log.warn("Cohort is null");
            return resultCohort;
        }
        List<FlaggedPatient> flaggedPatients = new ArrayList<>();
        DroolsEngineService droolsEngineService = Context.getService(DroolsEngineService.class);
        DroolsCalculationService calculationService = Context.getRegisteredComponents(DroolsCalculationService.class)
                .get(0);

        String sessionId = flag.getCriteria();
        String rule = flag.getName();
        KieSession session = droolsEngineService.requestSession(sessionId);
        session.setGlobal("flaggedPatients", flaggedPatients);
        session.setGlobal("calculationService", calculationService);
        List<FactHandle> patientFactHandles = new ArrayList<>();
        cohort.getActiveMemberships().stream().map(CohortMembership::getPatientId).forEach(id -> {
            Patient patient = Context.getPatientService().getPatient(id);
            FactHandle factHandle = session.insert(patient);
            patientFactHandles.add(factHandle);
        });
        // TODO: should we run the rules in a separate thread?
        session.fireAllRules(new AgendaFilter() {
            @Override
            public boolean accept(Match match) {
                String candidate = match.getRule().getName().trim();
                return candidate.equalsIgnoreCase(rule.trim());
            }
        });

        // Clean up the session
        // TODO: handle this from a "try-catch-finally block"
        patientFactHandles.forEach(factHandle -> {
            session.delete(factHandle);
        });

        for (FlaggedPatient flagged : flaggedPatients) {
            resultCohort.addMember(flagged.getPatientId());

            // Store message in context map if one was provided
            if (flagged.getMessage() != null && !flagged.getMessage().isEmpty()) {
                if (!contextMap.containsKey(flagged.getPatientId())) {
                    contextMap.put(flagged.getPatientId(), new ArrayList<String>());
                }

                @SuppressWarnings("unchecked")
                List<String> messages = (List<String>) contextMap.get(flagged.getPatientId());
                messages.add(flagged.getMessage());
            }
        }

        return resultCohort;
    }

    @Override
    public String evalMessage(Flag flag, int patientId) {
        // TODO figure out a more dynamic way to resolve flag messages
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

}
