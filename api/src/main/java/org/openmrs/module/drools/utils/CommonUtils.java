package org.openmrs.module.drools.utils;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openmrs.module.drools.session.ExternalEvaluatorManager;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Objects;

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
}
