package com.github.flexca.enot.core.types.asn1.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.EnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.SimpleElementSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Asn1SequenceSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {
        // TODO: check min and max size



        List<ASN1Encodable> asn1Elements = new ArrayList<>();
        for(ElementSerializationResult child : serializedBody) {
            if (!Asn1EnotValueType.ASN1_ELEMENT.equals(child.getValueType())) {
                throw new EnotSerializationException();
            }
            if(child.getData() instanceof ASN1Encodable asn1Child) {
                asn1Elements.add(asn1Child);
            } else {
                throw new EnotSerializationException();
            }
        }

        DERSequence sequence = new DERSequence(asn1Elements.toArray(new ASN1Encodable[0]));
        return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, sequence));
    }
}
