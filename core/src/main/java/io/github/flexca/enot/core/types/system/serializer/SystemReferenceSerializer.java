package io.github.flexca.enot.core.types.system.serializer;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;

import java.util.List;

public class SystemReferenceSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody, String jsonPath) throws EnotSerializationException {

        return serializedBody;
    }
}
