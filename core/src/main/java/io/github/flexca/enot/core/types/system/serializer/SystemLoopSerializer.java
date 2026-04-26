package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.BaseElementSerializer;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.context.ContextNode;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.types.system.attribute.Uniqueness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Uniqueness uniqueness = getUniqueness(loopElement);

        List<ElementSerializationResult> result = new ArrayList<>();
        Set<String> uniqueContextSha256 = new HashSet<>();
        while (context.hasNext()) {
            if (Uniqueness.ENFORCE.equals(uniqueness)) {
                ContextNode currentContext = context.getCurrentIterationNode();
                String currentContextSha256;
                try {
                    currentContextSha256 = currentContext.sha256Hex();
                } catch (Exception e) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                            "issue during uniqueness check, reason: " + e.getMessage()), e);
                }
                if (!uniqueContextSha256.add(currentContextSha256)) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                            "non unique values are not allowed"));
                }
            }
            result.addAll(serializeBody(loopElement.getBody(), context, jsonPath, enotContext));
            context.nextIndex();
        }
        context.pathStepBack();

        Object minObject = loopElement.getAttribute(SystemAttribute.MIN_ITEMS);
        if (minObject instanceof Number minNumber) {
            if (result.size() < minNumber.longValue()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "number of elements less than required minimum"));
            }
        }

        Object maxObject = loopElement.getAttribute(SystemAttribute.MAX_ITEMS);
        if (maxObject instanceof Number maxNumber) {
            if (result.size() > maxNumber.longValue()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "number of elements greater than required maximum"));
            }
        }

        return result;
    }

    private Uniqueness getUniqueness(EnotElement element) {

        Object uniquenessObject = element.getAttribute(SystemAttribute.UNIQUENESS);
        if (uniquenessObject == null) {
            return Uniqueness.NONE;
        }

        if (uniquenessObject instanceof String uniquenessString) {
            Uniqueness uniqueness = Uniqueness.fromName(uniquenessString);
            return uniqueness == null ? Uniqueness.NONE : uniqueness;
        }

        return Uniqueness.NONE;
    }
}
