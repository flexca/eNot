package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.BaseElementSerializer;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.List;
import java.util.Map;

public class SystemConditionSerializer extends BaseElementSerializer {

    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, Map<String, Object> parameters, String jsonPath, EnotRegistry enotRegistry) throws EnotSerializationException {

        Object expression = parameters.get(SystemAttribute.EXPRESSION);

        return null;
    }
}
