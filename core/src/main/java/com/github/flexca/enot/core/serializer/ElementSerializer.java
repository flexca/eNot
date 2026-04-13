package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.List;

public interface ElementSerializer {

    List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                               EnotContext enotContext) throws EnotSerializationException;
}
