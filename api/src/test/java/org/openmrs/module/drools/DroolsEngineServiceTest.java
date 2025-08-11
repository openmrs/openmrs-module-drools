/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.drools;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

@SuppressWarnings("deprecation")
public class DroolsEngineServiceTest extends BaseModuleContextSensitiveTest {

	@Autowired
	private DroolsEngineService droolsEngineService;

	PatientService patientService;
	ObsService obsService;
	ConceptService conceptService;

	@Before
	public void setup() throws Exception {
		patientService = Context.getPatientService();
		obsService = Context.getObsService();
		conceptService = Context.getConceptService();
		executeDataSet("org/openmrs/module/drools/testdata/DroolsServiceTest-dataset.xml");
	}

	@Test
	public void evaluate_shouldEvaluateRules() {
		Patient ethan = patientService.getPatient(200);
		KieSession session = droolsEngineService.evaluate("test1",
				Collections.singletonList(ethan));

		List<PatientFlag> flags = droolsEngineService.getSessionObjects(session,
				PatientFlag.class);

		assertThat(flags, hasSize(1));
		assertThat(flags, contains(new PatientFlag(ethan, "Hypertension", "High", null)));

		Patient noah = patientService.getPatient(202);
		Obs systolic = createObs(noah, 100, 85.0);
		Obs diastolic = createObs(noah, 101, 49.0);
		obsService.saveObs(systolic, null);
		obsService.saveObs(diastolic, null);

		session.insert(noah);
		session.fireAllRules();
		flags = droolsEngineService.getSessionObjects(session, PatientFlag.class);
		PatientFlag[] expectedFlags = {
				new PatientFlag(ethan, "Hypertension", "High", null),
				new PatientFlag(noah, "Hypotension", "Low", null)
		};
		assertThat(flags, hasSize(2));
		assertThat(flags, containsInAnyOrder(expectedFlags));

	}

	private Obs createObs(Patient patient, Integer conceptId, Double value) {
		Obs obs = new Obs();
		obs.setPerson(patient);
		obs.setConcept(conceptService.getConcept(conceptId));
		obs.setValueNumeric(value);
		obs.setLocation(new Location(1));
		obs.setObsDatetime(new Date());
		return obs;
	}

}
