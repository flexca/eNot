package io.github.flexca.enot.core.types.asn1.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.types.asn1.validation.Asn1ValidationUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.DEROctetString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Asn1OctetStringSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                        EnotJsonError.of(jsonPath, "missing required body for non optional element")));
            }
        }

        if (serializedBody.size() != 1) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.OCTET_STRING.getName()
                            + ", expected: 1, actual: " + serializedBody.size())));
        }

        if (CommonEnotValueType.BINARY.canConsume(serializedBody.get(0).getValueType())) {

            EnotBinaryConverter binaryConverter = serializedBody.get(0).getValueType().getBinaryConverter();
            if (binaryConverter == null) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "binary converter not found for type " + serializedBody.get(0).getValueType().getName()));
            }

            byte[] binaryInput;
            try {
                binaryInput = binaryConverter.toBinary(serializedBody.get(0).getData());
            } catch (Exception e) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "filed to convert input value to binary, reason: " + e.getMessage()), e);
            }

            String attributesPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
            List<EnotJsonError> jsonErrors = new ArrayList<>();
            Long minLength = Asn1ValidationUtils.validateAndExtractMinLength(element, attributesPath, jsonErrors);
            if (!jsonErrors.isEmpty()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, jsonErrors.get(0));
            }
            Long maxLength = Asn1ValidationUtils.validateAndExtractMaxLength(element, attributesPath, jsonErrors);
            if (!jsonErrors.isEmpty()) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, jsonErrors.get(0));
            }
            if (minLength != null) {
                if (binaryInput.length < minLength) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                            "body length is less than defined in " + Asn1Attribute.MIN_LENGTH.getName()));
                }
            }
            if (maxLength != null) {
                if (binaryInput.length > maxLength) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                            "body length is greater than defined in " + Asn1Attribute.MAX_LENGTH.getName()));
                }
            }
            return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                    new DEROctetString(binaryInput)));
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.OCTET_STRING.getName()
                        + ", expected: " + CommonEnotValueType.BINARY.getName())));
    }
}
