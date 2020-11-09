package com.scotiabankcolpatria.exceptions;

public class MandatoryFieldException extends Exception {

    public MandatoryFieldException(String message) {
        super(message);
    }

    public MandatoryFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
