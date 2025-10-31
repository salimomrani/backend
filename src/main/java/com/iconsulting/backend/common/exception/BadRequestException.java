package com.iconsulting.backend.common.exception;

/**
 * Exception levée lorsqu'une requête est invalide
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
