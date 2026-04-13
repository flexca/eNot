package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.serializer.BaseElementSerializer;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.ArrayList;
import java.util.List;

public class SystemLoopSerializer extends BaseElementSerializer {

    public List<ElementSerializationResult> serialize(EnotElement loopElement, SerializationContext context, String jsonPath,
                                                      EnotContext enotContext) throws EnotSerializationException {

        Object itemsNameValue = loopElement.getAttribute(SystemAttribute.ITEMS_NAME);
        if (itemsNameValue == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.ITEMS_NAME.getName(),
                    "attribute " + SystemAttribute.ITEMS_NAME.getName() + " must be set for system element loop"));
        }
        if (!(itemsNameValue instanceof String)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + SystemAttribute.ITEMS_NAME.getName(),
                    "attribute " + SystemAttribute.ITEMS_NAME.getName() + " must be string"));
        }

        context.pathStepForward((String) itemsNameValue);

        List<ElementSerializationResult> result = new ArrayList<>();
        while(context.hasNext()) {
            result.addAll(serializeBody(loopElement.getBody(), context, jsonPath, enotContext));
            context.nextIndex();
        }
        context.pathStepBack();

        Object minObject = loopElement.getAttribute(SystemAttribute.MIN_ITEMS);
        if(minObject instanceof Number minNumber) {
            if (result.size() < minNumber.longValue()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "number of elements less than required minimum"));
            }
        }

        Object maxObject = loopElement.getAttribute(SystemAttribute.MAX_ITEMS);
        if(maxObject instanceof Number maxNumber) {
            if (result.size() > maxNumber.longValue()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "number of elements greater than required maximum"));
            }
        }

        return result;
    }
}
