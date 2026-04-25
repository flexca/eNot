package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.BaseElementSerializer;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.List;

public class SystemGroupSerializer extends BaseElementSerializer {


    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                                      EnotContext enotContext) throws EnotSerializationException {

        Object groupNameValue = element.getAttribute(SystemAttribute.GROUP_NAME);
        if (groupNameValue == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.GROUP_NAME.getName(),
                    "attribute " + SystemAttribute.GROUP_NAME.getName() + " must be set for system element loop"));
        }
        if (!(groupNameValue instanceof String)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.GROUP_NAME.getName(),
                    "attribute " + SystemAttribute.GROUP_NAME.getName() + " must be string"));
        }

        context.pathStepForward((String) groupNameValue);

        List<ElementSerializationResult> result = serializeBody(element.getBody(), context, jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                enotContext);

        context.pathStepBack();

        return result;
    }
}
