package org.openmrs.module.drools.session;

import org.openmrs.api.APIException;

/**
 * Exception thrown when there are issues with Drools rule session operations.
 */
public class DroolsSessionException extends APIException {

    private static final long serialVersionUID = 1L;

    public DroolsSessionException() {
        super();
    }

    public DroolsSessionException(String message) {
        super(message);
    }

    public DroolsSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DroolsSessionException(Throwable cause) {
        super(cause);
    }
}
