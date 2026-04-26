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
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.util.OidUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Asn1ObjectIdentifierSerializer extends SimpleElementSerializer {

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
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.OBJECT_IDENTIFIER.getName()
                            + ", expected: 1, actual: " + serializedBody.size())));
        }

        if (CommonEnotValueType.OBJECT_IDENTIFIER.equals(serializedBody.get(0).getValueType()) || CommonEnotValueType.TEXT.equals(serializedBody.get(0).getValueType())) {
            if (serializedBody.get(0).getData() instanceof String textBody) {
                if (OidUtils.isValidOid(textBody)) {
                    Set<String> allowedValues = getAllowedValues(element);
                    if (CollectionUtils.isNotEmpty(allowedValues) && !allowedValues.contains(textBody)) {
                        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                                EnotJsonError.of(jsonPath, "value is not in a list of " + Asn1Attribute.ALLOWED_VALUES.getName())));
                    }
                    return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, new ASN1ObjectIdentifier(textBody)));
                } else {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                            EnotJsonError.of(jsonPath, "body must be valid OID")));
                }
            }
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, Collections.singletonList(
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.OBJECT_IDENTIFIER.getName()
                        + ", expected: " + CommonEnotValueType.OBJECT_IDENTIFIER.getName() + " or " + CommonEnotValueType.TEXT.getName())));

    }

    private Set<String> getAllowedValues(EnotElement element) {

        Object allowedValuesObject = element.getAttribute(Asn1Attribute.ALLOWED_VALUES);
        if (allowedValuesObject == null) {
            return Collections.emptySet();
        }

        Set<String> allowedValues = new HashSet<>();
        if (allowedValuesObject instanceof Collection<?> allowedValuesCollection) {
            for(Object item : allowedValuesCollection) {
                if (item instanceof String itemString) {
                    allowedValues.add(itemString);
                }
            }
        } else if (allowedValuesObject instanceof String allowedValuesString) {
            allowedValues.add(allowedValuesString);
        }

        return allowedValues;
    }
}
