package com.github.flexca.enot.core.types.asn1.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.SimpleElementSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import org.apache.commons.collections4.CollectionUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Asn1SetSerialized extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, new DERSet()));
            }
        }

        List<ASN1Encodable> asn1Elements = new ArrayList<>();
        for(ElementSerializationResult child : serializedBody) {
            if (!Asn1EnotValueType.ASN1_ELEMENT.equals(child.getValueType())) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "expecting body of type " + Asn1EnotValueType.ASN1_ELEMENT.getName()));
            }
            if(child.getData() instanceof ASN1Encodable asn1Child) {
                asn1Elements.add(asn1Child);
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "expecting data to be ASN1Encodable for body of type " + Asn1EnotValueType.ASN1_ELEMENT.getName()));
            }
        }

        DERSet set = new DERSet(asn1Elements.toArray(new ASN1Encodable[0]));
        return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, set));
    }
}
