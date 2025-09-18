package org.openmrs.module.drools.web.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.openmrs.module.drools.api.DroolsEngineService;
import org.openmrs.module.drools.param.DroolsParameterDefinition;
import org.openmrs.module.drools.session.DroolsSessionConfig;
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
        sessionExecutor.executeSessionAsync(sessionId, allParams)
                .thenApply(result -> convertToSimpleObject(sessionExecutor.executeSession(sessionId, allParams), request))
                .exceptionally(ex -> {
                    // Handle exceptions
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
                });
        return convertToSimpleObject(sessionExecutor.executeSession(sessionId, allParams), request);
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

