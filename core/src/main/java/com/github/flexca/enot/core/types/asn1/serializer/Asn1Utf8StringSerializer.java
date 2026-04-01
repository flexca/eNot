package com.github.flexca.enot.core.types.asn1.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.SimpleElementSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import com.github.flexca.enot.core.types.asn1.Asn1Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.DERUTF8String;

import java.util.Collections;
import java.util.List;

public class Asn1Utf8StringSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException{

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
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.UTF8_STRING.getName()
                            + ", expected: 1, actual: " + serializedBody.size())));
        }

        if (CommonEnotValueType.TEXT.equals(serializedBody.get(0).getValueType())) {
            String textBody = serializedBody.get(0).getData();
            return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, new DERUTF8String(textBody)));
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                    EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.UTF8_STRING.getName()
                            + ", expected: " + CommonEnotValueType.TEXT.getName())));
        }
    }
}
