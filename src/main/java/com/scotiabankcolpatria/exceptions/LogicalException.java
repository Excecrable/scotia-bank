package com.scotiabankcolpatria.exceptions;

public class LogicalException extends Exception {

    public LogicalException(String message) {
        super(message);
    }

    public LogicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
