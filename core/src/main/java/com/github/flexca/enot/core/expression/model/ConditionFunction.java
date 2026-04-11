package com.github.flexca.enot.core.expression.model;

import com.github.flexca.enot.core.exception.EnotExpressionEvaluationException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ConditionFunction {

    DATE_TIME("date_time", 1) {
        @Override
        public Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException {
            return null;
        }
    },
    LENGTH("length", 1) {
        @Override
        public Object evaluate(List<Object> arguments) throws EnotExpressionEvaluationException {
            return null;
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
