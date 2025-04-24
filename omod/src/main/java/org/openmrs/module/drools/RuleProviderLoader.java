package org.openmrs.module.drools;

import java.util.List;

import org.openmrs.module.drools.api.RuleProvider;

/**
 * This interface is used to load rule providers. Rule providers are responsible
 * for
 * providing rules and session configurations.
 */
public interface RuleProviderLoader {

    List<RuleProvider> loadRuleProviders();
}
