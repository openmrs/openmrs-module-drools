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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.drools.event.DroolsEventsManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class OpenMRSDroolsEngineActivator extends BaseModuleActivator implements DaemonTokenAware {

	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private DroolsEventsManager eventsManager;

	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started OpenMRS Drools Engine");
	}

	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		// TODO: Add shutdown logic here
		// eventsManager.unSubscribeAll();
		log.info("OpenMRS Drools Engine stopped");
	}

	@Override
	public void setDaemonToken(DaemonToken token) {
		eventsManager.setDaemonToken(token);
	}

}
