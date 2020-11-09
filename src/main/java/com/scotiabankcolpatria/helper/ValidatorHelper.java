package com.scotiabankcolpatria.helper;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ValidatorHelper {

    private static final Predicate<Object> IS_NULL = Objects::isNull;
    private static final Predicate<Object> IS_EMPTY = obj -> ((obj == null) || (((String) obj).trim().isEmpty()));
    private static final Predicate<Object> IS_NEGATIVE = obj -> ((obj != null) && (((Double) obj) < 0));
    private static final Predicate<Object> IS_ACCOUNT_PATTERN = obj -> ((obj == null) || (!Pattern.matches("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}", ((String) obj))));

    private static final Map<Validations, Predicate<Object>> VALIDATIONS = Map.of(
            Validations.NULL, IS_NULL,
            Validations.EMPTY, IS_EMPTY,
            Validations.NEGATIVE, IS_NEGATIVE,
            Validations.ACCOUNT_PATTERN, IS_ACCOUNT_PATTERN
    );

    private ValidatorHelper() {
        super();
    }

    public static boolean validate(Object value, @NotNull Validations... validations) {
        boolean isValid = Boolean.FALSE;

        // Aplicar las validaciones
        for (Validations val : validations) {
            if (VALIDATIONS.get(val).test(value)) {
                isValid = Boolean.TRUE;
                break;
            }
        }

        return isValid;
    }

    public enum Validations {
        NULL, EMPTY, NEGATIVE, ACCOUNT_PATTERN
    }


}
