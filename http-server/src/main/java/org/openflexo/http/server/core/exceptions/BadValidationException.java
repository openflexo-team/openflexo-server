package org.openflexo.http.server.core.exceptions;

/**
 * A class to declare custom Bad validation exception.
 */
public class BadValidationException extends Exception{
    /**
     * Instantiates a new Bad validation exception.
     *
     * @param errorMessage the error message
     */
    public BadValidationException(String errorMessage) {
        super(errorMessage);
    }
}
