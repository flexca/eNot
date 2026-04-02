package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.registry.EnotRegistry;

import java.util.List;
import java.util.Map;

public interface ElementSerializer {

    List<ElementSerializationResult> serialize(EnotElement element, Map<String, Object> parameters, String jsonPath,
                                               EnotRegistry enotRegistry) throws EnotSerializationException;
}
