package com.github.flexca.enot.core.types.asn1.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.registry.EnotBinaryConverter;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.SimpleElementSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import com.github.flexca.enot.core.types.asn1.Asn1Tag;
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.DERBitString;

import java.util.Collections;
import java.util.List;

public class Asn1BitStringSerializer extends SimpleElementSerializer {

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
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.BIT_STRING.getName()
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
            } catch(Exception e) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "filed to convert input value to binary, reason: " + e.getMessage()), e);
            }

            Object applyPaddingObject = element.getAttribute(Asn1Attribute.APPLY_PADDING);
            if (applyPaddingObject == null) {
                applyPaddingObject = Boolean.FALSE;
            }
            if(applyPaddingObject instanceof Boolean applyPadding) {
                if (applyPadding) {
                    byte[] trimmed = trim(binaryInput);
                    int padding = calculatePadding(trimmed);
                    return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                            new DERBitString(trimmed, padding)));
                } else {
                    return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                            new DERBitString(binaryInput)));
                }
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                                + "/" + Asn1Attribute.APPLY_PADDING.getName(), "invalid value for attribute "
                        + Asn1Attribute.APPLY_PADDING.getName()));
            }

        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.BIT_STRING.getName()
                        + ", expected: " + CommonEnotValueType.BINARY.getName())));
    }

    private byte[] trim(byte[] input) {
        if(input.length <= 1) {
            return input;
        }

        int index = input.length;
        for (int i = input.length - 1; i > 0; i--) {
            if(input[i] != 0) {
                break;
            } else {
                index = i;
            }
        }
        byte[] trimmed = new byte[index];
        System.arraycopy(input, 0, trimmed, 0, index);
        return trimmed;
    }

    private int calculatePadding(byte[] input) {
        int padding = -1;
        for(int i = 0; i < 8; i++) {
            if((input[input.length - 1] & (1 << i)) > 0) {
                padding = i;
                break;
            }
        }
        if(padding == -1) {
            padding = 7;
        }
        return padding;
    }
}
