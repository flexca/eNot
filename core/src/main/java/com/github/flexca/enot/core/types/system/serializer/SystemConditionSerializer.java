package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.serializer.BaseElementSerializer;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.Collections;
import java.util.List;

public class SystemConditionSerializer extends BaseElementSerializer {

    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                                      EnotContext enotContext) throws EnotSerializationException {

        Object expression = element.getAttribute(SystemAttribute.EXPRESSION);
        if (expression == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.EXPRESSION.getName(),
                    "attribute " + SystemAttribute.EXPRESSION.getName() + " must be set for system element condition"));
        }

        if(!(expression instanceof String)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.EXPRESSION.getName(),
                    "attribute " + SystemAttribute.EXPRESSION.getName() + " must be string"));
        }

        boolean evaluationResult;
        try {
            evaluationResult = enotContext.getConditionExpressionEvaluator().evaluate((String) expression, context);
        } catch (Exception e) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.EXPRESSION.getName(),
                    e.getMessage()), e);
        }

        if (!evaluationResult) {
            return Collections.emptyList();
        }

        return serializeBody(element.getBody(), context, jsonPath, enotContext);
    }
}
