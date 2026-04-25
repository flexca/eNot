package io.github.flexca.enot.core.types.asn1.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.types.asn1.Asn1EnotValueType;
import org.bouncycastle.asn1.DERNull;

import java.util.Collections;
import java.util.List;

public class Asn1NullSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        return Collections.singletonList(ElementSerializationResult.of(Asn1EnotValueType.ASN1_ELEMENT, DERNull.INSTANCE));
    }
}
