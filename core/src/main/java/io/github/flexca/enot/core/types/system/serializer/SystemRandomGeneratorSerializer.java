package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.ElementSerializer;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.types.system.validation.SystemRandomGeneratorValidator;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

public class SystemRandomGeneratorSerializer implements ElementSerializer {

    private static final char[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final char[] CHARS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final char[] CHARS_CAPS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private static final char[] ALPHA_NUMERIC;

    static {
        ALPHA_NUMERIC = new char[DIGITS.length + CHARS.length + CHARS_CAPS.length];
        System.arraycopy(DIGITS, 0, ALPHA_NUMERIC, 0, DIGITS.length);
        System.arraycopy(CHARS, 0, ALPHA_NUMERIC, DIGITS.length, CHARS.length);
        System.arraycopy(CHARS_CAPS, 0, ALPHA_NUMERIC, DIGITS.length + CHARS.length, CHARS_CAPS.length);
    }

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath, EnotContext enotContext) throws EnotSerializationException {

        Object minObject = element.getAttribute(SystemAttribute.RANDOM_STRING_LENGTH);
        if (minObject instanceof Number randomStringLength) {
            long requestedLength = randomStringLength.longValue();
            if (requestedLength <= 0) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                        + " must be greater than zero"));
            } else if (requestedLength > SystemRandomGeneratorValidator.ABSOLUTE_MAX_LENGTH) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                        + " must be less or equals to " + SystemRandomGeneratorValidator.ABSOLUTE_MAX_LENGTH));
            }
            String randomValue = alphaNumericRandom(requestedLength);
            return Collections.singletonList(ElementSerializationResult.of(CommonEnotValueType.TEXT, randomValue));
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                    + " must be of type integer"));
        }
    }

    private String alphaNumericRandom(long length) {

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(ALPHA_NUMERIC.length);
            builder.append(ALPHA_NUMERIC[index]);
        }
        return builder.toString();
    }
}
