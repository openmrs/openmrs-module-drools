package org.openmrs.module.drools.web.response;

import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Requested Drools Session doesn't exist or it's disabled")
public class SessionConfigurationNotFoundException extends ResponseException {
    private static final long serialVersionUID = 1L;

    public SessionConfigurationNotFoundException() {
    }

    public SessionConfigurationNotFoundException(String message) {
        super(message);
    }

    public SessionConfigurationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionConfigurationNotFoundException(Throwable cause) {
        super(cause);
    }
}
