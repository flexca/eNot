package io.github.flexca.enot.core.types.asn1.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.types.asn1.validation.Asn1ValidationUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.DERPrintableString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Asn1PrintableStringSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody, String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "missing required body for non optional element"));
            }
        }

        if (serializedBody.size() != 1) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.PRINTABLE_STRING.getName()
                            + ", expected: 1, actual: " + serializedBody.size()));
        }

        if (CommonEnotValueType.TEXT.equals(serializedBody.get(0).getValueType())) {
            if (serializedBody.get(0).getData() instanceof String textBody) {
                if (!ASN1PrintableString.isPrintableString(textBody)) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                            EnotJsonError.of(jsonPath, "invalid text input for ASN.1 " + Asn1Tag.PRINTABLE_STRING.getName()));
                }

                Asn1ValidationUtils.validateMinAndMaxLengthForTextDuringSerialization(element, jsonPath, textBody);
                Asn1ValidationUtils.validateAllowedValuesForTextDuringSerialization(element, jsonPath, textBody);

                return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                        new DERPrintableString(textBody)));
            }
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.PRINTABLE_STRING.getName()
                        + ", expected: " + CommonEnotValueType.TEXT.getName()));
    }
}
