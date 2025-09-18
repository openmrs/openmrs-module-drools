package org.openmrs.module.drools.web;

import org.openmrs.api.context.Context;
import org.openmrs.module.drools.session.DroolsExecutionResult;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

public class RestUtil {

    public static final String DEFAULT_REPRESENTATION = "(uuid,display)";

    public static SimpleObject convertToSimpleObject(DroolsExecutionResult result, HttpServletRequest request) {
        SimpleObject simpleObject = new SimpleObject();
        Representation representation = getRepresentation(request);
        simpleObject.add("sessionId", result.getSessionId());
        simpleObject.add("firedRulesCount", result.getFiredRulesCount());
        simpleObject.add("results", result.getResults().stream().map(object -> ConversionUtil.convertToRepresentation(object, representation)).collect(Collectors.toList()));
        return simpleObject;
    }

    private static Representation getRepresentation(HttpServletRequest request) {
        Representation representation = new CustomRepresentation(DEFAULT_REPRESENTATION);
        // get the "v" param for the representations
        String customRepresentation = request.getParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION);
        if (customRepresentation != null && !customRepresentation.isEmpty() && !RestConstants.REPRESENTATION_DEFAULT.equalsIgnoreCase(customRepresentation)) {
            representation = Context.getService(RestService.class).getRepresentation(customRepresentation);
        }
        return representation;
    }
}
