package com.scotiabankcolpatria.exceptions;

public class EntityFoundException extends Exception {

    public EntityFoundException(String message) {
        super(message);
    }

    public EntityFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
