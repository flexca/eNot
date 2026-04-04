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
import com.github.flexca.enot.core.util.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERUTCTime;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class Asn1UtcTimeSerializer extends SimpleElementSerializer {

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
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.UTC_TIME.getName()
                            + ", expected: 1, actual: " + serializedBody.size())));
        }

        if (CommonEnotValueType.TEXT.equals(serializedBody.get(0).getValueType()) || CommonEnotValueType.DATE_TIME.equals(serializedBody.get(0).getValueType())) {

            try {
                ZonedDateTime input = null;
                if (serializedBody.get(0).getData() instanceof String stringBody) {
                    input = DateTimeUtils.toZonedDateTime(stringBody);
                } if (serializedBody.get(0).getData() instanceof ZonedDateTime dateTimeBody) {
                    input = dateTimeBody;
                }

                if (input != null) {
                    String asn1Input = DateTimeUtils.formatForAsn1(input);
                    return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                            new DERUTCTime(asn1Input)));
                }

            } catch (Exception e) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                        EnotJsonError.of(jsonPath, "failure during serialization of ASN.1 element " + Asn1Tag.UTC_TIME.getName()
                                + ", reason: " + e.getMessage()), e);
            }
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.UTC_TIME.getName()
                        + ", expected: " + CommonEnotValueType.TEXT.getName() + " or " + CommonEnotValueType.DATE_TIME.getName())));
    }
}
