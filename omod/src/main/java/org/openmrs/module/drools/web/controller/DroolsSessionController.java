package org.openmrs.module.drools.web.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.session.DroolsSessionConfig;
import org.openmrs.module.drools.session.SessionMetadata;
import org.openmrs.module.drools.session.ThreadSafeSessionRegistry;
import org.openmrs.module.drools.web.DroolsSessionExecutor;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openmrs.module.drools.web.RestUtil.convertToSimpleObject;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + DroolsSessionController.DROOLS_REST_NAMESPACE)
public class DroolsSessionController extends BaseRestController {

    public static final String DROOLS_REST_NAMESPACE = "/drools";

    @Autowired
    private DroolsEngineService droolsService;

    @Autowired
    private DroolsSessionExecutor sessionExecutor;

    @Autowired
    private ThreadSafeSessionRegistry sessionRegistry;



    @Override
    public String getNamespace() {
        return RestConstants.VERSION_1 + DROOLS_REST_NAMESPACE;
    }

    @RequestMapping(value = "/rule/{sessionId}", method = RequestMethod.POST)
    @ResponseBody
    public SimpleObject executeRulesSession(
            @PathVariable("sessionId") String sessionId,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest request,
            HttpServletResponse response) throws ResponseException {

        DroolsSessionConfig sessionConfig = droolsService.getSessionConfig(sessionId);
        if (sessionConfig == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Drools Session with ID '%s' does not exist or is disabled", sessionId)
            );
        }
        // validate params
        validateParams(sessionConfig, allParams);
        // FIX: Remove duplicate executeSession call - execute only once
        return convertToSimpleObject(sessionExecutor.executeSession(sessionId, allParams), request);
    }

    /**
     * GET /ws/rest/v1/drools/rules
     * Retrieves information about all active sessions in the registry.
     *
     * @return SimpleObject containing activeIds, autoStartableIds, and totalCount
     */
    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getActiveSessions() {
        Collection<String> activeIds = sessionRegistry.getActiveSessionIds();
        Collection<String> autoStartableIds = sessionRegistry.getAutoStartableSessionIds();
        long totalCount = sessionRegistry.getActiveSessionCount();

        SimpleObject result = new SimpleObject();
        result.add("activeSessionIds", activeIds);
        result.add("autoStartableSessionIds", autoStartableIds);
        result.add("totalCount", totalCount);
        return result;
    }

    /**
     * GET /ws/rest/v1/drools/rules/{sessionId}
     * Retrieves detailed metadata for a specific session.
     *
     * @param sessionId the session identifier
     * @return SimpleObject with session metadata
     */
    @RequestMapping(value = "/rules/{sessionId}", method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getSessionDetails(@PathVariable("sessionId") String sessionId) {
        SessionMetadata metadata = sessionRegistry.getSessionMetadata(sessionId);
        if (metadata == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Session '%s' not found in registry", sessionId)
            );
        }

        SimpleObject result = new SimpleObject();
        result.add("sessionId", metadata.getSessionId());
        result.add("autoStartable", metadata.isAutoStartable());
        result.add("createdAt", metadata.getCreatedAt().toString());
        result.add("createdByThread", metadata.getCreatedByThread());
        result.add("lastAccessed", metadata.getLastAccessed().toString());
        result.add("accessCount", metadata.getAccessCount());
        return result;
    }

    /**
     * POST /ws/rest/v1/drools/rules/{sessionId}/execute
     * Executes rules against an existing session in the registry.
     * Only works for auto-startable sessions that are already registered.
     *
     * @param sessionId the session identifier
     * @param allParams execution parameters
     * @param request HTTP request
     * @return SimpleObject with execution results
     */
    @RequestMapping(value = "/rules/{sessionId}/execute", method = RequestMethod.POST)
    @ResponseBody
    public SimpleObject executeAgainstSession(
            @PathVariable("sessionId") String sessionId,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest request) {

        if (!sessionRegistry.sessionExists(sessionId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Session '%s' not found in registry. Only auto-startable sessions can be queried.", sessionId)
            );
        }

        DroolsSessionConfig sessionConfig = droolsService.getSessionConfig(sessionId);
        if (sessionConfig == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Session configuration for '%s' not found", sessionId)
            );
        }

        validateParams(sessionConfig, allParams);
        return convertToSimpleObject(
                sessionExecutor.executeAgainstExistingSession(sessionId, allParams),
                request
        );
    }

    /**
     * DELETE /ws/rest/v1/drools/rules/{sessionId}
     * Removes and disposes a session from the registry.
     *
     * @param sessionId the session identifier
     * @return SimpleObject with success status
     */
    @RequestMapping(value = "/rules/{sessionId}", method = RequestMethod.DELETE)
    @ResponseBody
    public SimpleObject disposeSession(@PathVariable("sessionId") String sessionId) {
        boolean removed = sessionRegistry.removeSession(sessionId);

        SimpleObject result = new SimpleObject();
        result.add("sessionId", sessionId);
        result.add("removed", removed);
        result.add("message", removed
                ? String.format("Session '%s' successfully removed and disposed", sessionId)
                : String.format("Session '%s' not found in registry", sessionId)
        );
        return result;
    }

    /**
     * GET /ws/rest/v1/drools/rules/{sessionId}/exists
     * Checks if a session exists in the registry.
     *
     * @param sessionId the session identifier
     * @return SimpleObject with existence status
     */
    @RequestMapping(value = "/rules/{sessionId}/exists", method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject sessionExists(@PathVariable("sessionId") String sessionId) {
        boolean exists = sessionRegistry.sessionExists(sessionId);

        SimpleObject result = new SimpleObject();
        result.add("sessionId", sessionId);
        result.add("exists", exists);
        return result;
    }

    private void validateParams(DroolsSessionConfig sessionConfig, Map<String, String> params) {
        List<String> missingParams = new ArrayList<>();
        List<String> requiredParams = sessionConfig.getParameterDefinitions().stream().filter(DroolsParameterDefinition::getRequired).map(DroolsParameterDefinition::getParameterName).collect(Collectors.toList());
        for (String param: requiredParams) {
            if (ObjectUtils.isEmpty(params.get(param))) {
                missingParams.add(param);
            }
        }
        if (!missingParams.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter(s): " + Arrays.toString(missingParams.toArray()));
        }
    }

}

