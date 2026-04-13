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
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERTaggedObject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Asn1TaggedObjectSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody, String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                        EnotJsonError.of(jsonPath, "missing required body for non optional element"));
            }
        }

        if (serializedBody.size() != 1) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                    EnotJsonError.of(jsonPath, "unexpected size of arguments for " + Asn1Tag.TAGGED_OBJECT.getName()
                            + ", expected: 1, actual: " + serializedBody.size()));
        }

        Optional<Integer> implicit = extractIntegerAttribute(element, Asn1Attribute.IMPLICIT);
        Optional<Integer> explicit = extractIntegerAttribute(element, Asn1Attribute.EXPLICIT);
        if(implicit.isPresent() && explicit.isPresent()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                    EnotJsonError.of(jsonPath, "for ASN.1 element " + Asn1Tag.TAGGED_OBJECT.getName()
                            + " only one of  " + Asn1Attribute.IMPLICIT.getName() + " or " + Asn1Attribute.EXPLICIT.getName()
                            + " attributes must be present"));
        }
        if(implicit.isEmpty() && explicit.isEmpty()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                    EnotJsonError.of(jsonPath, "for ASN.1 element " + Asn1Tag.TAGGED_OBJECT.getName()
                            + " at least one of  " + Asn1Attribute.IMPLICIT.getName() + " or " + Asn1Attribute.EXPLICIT.getName()
                            + " attributes must be present"));
        }

        boolean explicitValue = explicit.isPresent();
        int taggedValue = explicit.isPresent() ? explicit.get() : implicit.get();
        if(taggedValue <= 0) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                    EnotJsonError.of(jsonPath, "for ASN.1 element " + Asn1Tag.TAGGED_OBJECT.getName()
                            + " value of  " + Asn1Attribute.IMPLICIT.getName() + " or " + Asn1Attribute.EXPLICIT.getName()
                            + " must be positive integer greater than zero"));
        }

        if (Asn1EnotValueType.ASN1_ELEMENT.equals(serializedBody.get(0).getValueType())) {
            if (serializedBody.get(0).getData() instanceof ASN1Encodable asn1Body) {
                return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT,
                        new DERTaggedObject(explicitValue, taggedValue, asn1Body)));
            }
        }

        throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE,
                EnotJsonError.of(jsonPath, "unsupported body type for " + Asn1Tag.TAGGED_OBJECT.getName()
                        + ", expected: " + CommonEnotValueType.OBJECT_IDENTIFIER.getName() + " or "+ Asn1EnotValueType.ASN1_ELEMENT.getName()));
    }

    private Optional<Integer> extractIntegerAttribute(EnotElement element, Asn1Attribute attributeName) {
        if (element.getAttributes() == null) {
            return Optional.empty();
        }
        Object attributeValue = element.getAttributes().get(attributeName);
        if(attributeValue == null) {
            return Optional.empty();
        }
        if(attributeValue instanceof Number numberValue) {
            return Optional.of(numberValue.intValue());
        }
        return Optional.empty();
    }
}
