package io.github.flexca.enot.core.types.asn1.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1Integer;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class Asn1IntegerSerializer extends SimpleElementSerializer {

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
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.INTEGER.getName()
                            + ", expected: 1, actual: " + serializedBody.size())));
        }

        if (CommonEnotValueType.INTEGER.equals(serializedBody.get(0).getValueType())) {
            if(serializedBody.get(0).getData() instanceof Integer integerBody) {
                return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                        ASN1Integer.getInstance(integerBody)));
            } else if(serializedBody.get(0).getData() instanceof Long longBody) {
                return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                        ASN1Integer.getInstance(longBody)));
            } else if(serializedBody.get(0).getData() instanceof BigInteger bigIntegerBody) {
                return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                        ASN1Integer.getInstance(bigIntegerBody)));
            }
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.INTEGER.getName()
                        + ", expected: " + CommonEnotValueType.INTEGER.getName())));
    }
}
