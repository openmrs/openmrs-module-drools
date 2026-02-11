package org.openmrs.module.drools.utils;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.api.context.Context;
import org.openmrs.module.drools.session.ExternalEvaluatorManager;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommonUtils {

    private static Logger log = LoggerFactory.getLogger(CommonUtils.class);

    public static KieSession createKieSession(KieContainer container, DroolsSessionConfig config,
            ExternalEvaluatorManager evaluatorManager, Map<String, Map<String, Object>> globalBindings) {
        log.debug("Creating new KieSession");
        KieSession session = container.newKieSession();
        if (config != null) {
            globalBindings.forEach((sessionId, globals) -> {
                if (!sessionId.equals(config.getSessionId())) {
                    globals.forEach(session::setGlobal);
                }
            });
            if (config.getGlobals() != null) {
                log.debug("Setting " + config.getGlobals().size() + " globals on KieSession");
                config.getGlobals().forEach(session::setGlobal);
            }
            if (config.getSessionRuntimeEventListeners() != null) {
                log.debug("Adding " + config.getSessionRuntimeEventListeners().size() + " runtime event listeners to KieSession");
                config.getSessionRuntimeEventListeners().forEach(session::addEventListener);
            }
        } else {
            log.debug("RuleSessionConfig is null; no globals or listeners set");
        }
        session.setGlobal("evaluatorManager", evaluatorManager);
        return session;
    }

    public static void removeFactsByClass(KieSession kieSession, Class<?> factClass) {
        log.debug("Removing facts of type: " + factClass.getName());
        kieSession.getObjects().stream()
                .filter(factClass::isInstance)
                .forEach(fact -> kieSession.delete(kieSession.getFactHandle(fact)));
    }

    /**
     * Converts rules from an Excel spreadsheet to a Drools Rule Language (DRL)
     * file.
     * 
     * @param excelFilePath The path to the Excel file containing rules
     * @param outputDrlPath The path where the generated DRL file should be saved
     * @throws IOException If there is an error reading the Excel file or writing
     *                     the DRL file
     */
    public static void convertExcelRulesToDrl(String excelFilePath, String outputDrlPath) throws IOException {
        Objects.requireNonNull(excelFilePath, "Excel file path cannot be null");
        Objects.requireNonNull(outputDrlPath, "Output DRL path cannot be null");

        InputStream excelInputStream = null;

        try {
            // Try loading from file system first
            excelInputStream = new FileInputStream(excelFilePath);
        } catch (FileNotFoundException fileEx) {
            // If not found, try loading from classpath
            excelInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(excelFilePath);

            if (excelInputStream == null) {
                throw new IOException("Excel file not found in filesystem or classpath: " + excelFilePath, fileEx);
            }

        }

        try (FileWriter drlFileWriter = new FileWriter(outputDrlPath)) {

            SpreadsheetCompiler spreadsheetCompiler = new SpreadsheetCompiler();
            String drlContent = spreadsheetCompiler.compile(excelInputStream, InputType.XLS);
            drlFileWriter.write(drlContent);
        }

    }

    /**
     * Gets a concept by its SAME-AS mapping to an external source.
     * Unlike ConceptService.getConceptByMapping(), this method only considers
     * SAME-AS mappings, avoiding the "Multiple non-retired concepts found" error
     * that occurs when a concept has both SAME-AS and NARROWER-THAN mappings.
     *
     * @param code The concept code in the external source
     * @param sourceName The name of the concept source (e.g., "CIEL")
     * @return The concept with a SAME-AS mapping to the given code, or null if not found
     */
    public static Concept getConceptBySameAsMapping(String code, String sourceName) {
        if (code == null || sourceName == null) {
            return null;
        }

        List<Concept> concepts = Context.getConceptService().getConceptsByMapping(code, sourceName, false);
        if (concepts == null || concepts.isEmpty()) {
            return null;
        }

        // Filter for SAME-AS mapping type only
        for (Concept concept : concepts) {
            for (ConceptMap mapping : concept.getConceptMappings()) {
                if (mapping.getConceptReferenceTerm() != null
                        && mapping.getConceptReferenceTerm().getConceptSource() != null
                        && sourceName.equals(mapping.getConceptReferenceTerm().getConceptSource().getName())
                        && code.equals(mapping.getConceptReferenceTerm().getCode())) {
                    ConceptMapType mapType = mapping.getConceptMapType();
                    if (mapType != null && "SAME-AS".equalsIgnoreCase(mapType.getName())) {
                        return concept;
                    }
                }
            }
        }

        // Fall back to first result if no SAME-AS found (backwards compatibility)
        log.warn("No SAME-AS mapping found for code {} from source {}, returning first match", code, sourceName);
        return concepts.get(0);
    }

    /**
     * Gets all concepts that have a SAME-AS or NARROWER-THAN mapping to the given code.
     * This is useful for finding concepts that represent the same or more specific
     * versions of a given terminology code (e.g., SNOMED CT).
     *
     * @param code The concept code in the external source
     * @param sourceName The name of the concept source (e.g., "SNOMED CT")
     * @return A set of concepts with SAME-AS or NARROWER-THAN mappings, or empty set if none found
     */
    public static Set<Concept> getConceptsBySameAsOrNarrowerThanMapping(String code, String sourceName) {
        Set<Concept> result = new HashSet<>();
        if (code == null || sourceName == null) {
            return result;
        }

        List<Concept> concepts = Context.getConceptService().getConceptsByMapping(code, sourceName, false);
        if (concepts == null || concepts.isEmpty()) {
            return result;
        }

        for (Concept concept : concepts) {
            for (ConceptMap mapping : concept.getConceptMappings()) {
                if (mapping.getConceptReferenceTerm() != null
                        && mapping.getConceptReferenceTerm().getConceptSource() != null
                        && sourceName.equals(mapping.getConceptReferenceTerm().getConceptSource().getName())
                        && code.equals(mapping.getConceptReferenceTerm().getCode())
                        && mapping.getConceptMapType() != null) {
                    String mapType = mapping.getConceptMapType().getName();
                    if ("SAME-AS".equalsIgnoreCase(mapType) || "NARROWER-THAN".equalsIgnoreCase(mapType)) {
                        result.add(concept);
                        break;
                    }
                }
            }
        }

        return result;
    }
}
