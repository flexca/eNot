package com.github.flexca.enot.core.expression.model;

import com.github.flexca.enot.core.exception.EnotExpressionEvaluationException;
import com.github.flexca.enot.core.util.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Array;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ConditionFunction {

    DATE_TIME("date_time", 1) {
        @Override
        public Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException {

            if (CollectionUtils.isEmpty(arguments) || arguments.size() != 1) {
                throw new EnotExpressionEvaluationException("expected only one arguments for date_time function");
            }

            Object argumentObject = arguments.get(0);
            if (argumentObject instanceof ZonedDateTime zonedDateTimeArgument) {
                return zonedDateTimeArgument;
            } else if (argumentObject instanceof String stringArgument) {
                try {
                    return DateTimeUtils.toZonedDateTime(stringArgument);
                } catch(Exception e) {
                    throw new EnotExpressionEvaluationException("failure during converting of string to datetime, reason: "
                            + e.getMessage(), e);
                }
            }
            throw new EnotExpressionEvaluationException("unsupported argument type for date_time function, expected: " +
                    "String or ZonedDatetime");
        }
    },
    LENGTH("length", 1) {
        @Override
        public Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException {

            if (CollectionUtils.isEmpty(arguments) || arguments.size() != 1) {
                throw new EnotExpressionEvaluationException("expected only one arguments for length function");
            }

            Object argumentObject = arguments.get(0);
            if(argumentObject == null) {
                return 0;
            }
            if (argumentObject instanceof String stringArgument) {
                return stringArgument.length();
            } else if (argumentObject instanceof Collection<?> collectionArgument) {
                return collectionArgument.size();
            } else if (argumentObject.getClass().isArray()) {
                return Array.getLength(argumentObject);
            }
            throw new EnotExpressionEvaluationException("unsupported argument type for length function, expected: " +
                    "String, Collection or array");
        }
    },
    IS_NULL("is_null", 1) {
        @Override
        public Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException {

            if (CollectionUtils.isEmpty(arguments) || arguments.size() != 1) {
                throw new EnotExpressionEvaluationException("expected only one arguments for is_null function");
            }

            Object argumentObject = arguments.get(0);
            return argumentObject == null;
        }
    };

    private static final Map<String, ConditionFunction> BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_NAME.put(value.getName(), value));
    }

    private final String name;
    private final int argumentsNumber;

    private ConditionFunction(String name, int argumentsNumber) {
        this.name = name;
        this.argumentsNumber = argumentsNumber;
    }

    public abstract Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException;

    public String getName() {
        return name;
    }

    public int getArgumentsNumber() {
        return argumentsNumber;
    }

    public static ConditionFunction getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
