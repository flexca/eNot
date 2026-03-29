package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.registry.EnotRegistry;

import java.util.List;
import java.util.Map;

public interface ElementSerializer {

    List<Object> serialize(EnotElement element, List<Object> input, Map<String, Object> parameters, String parametersPath, EnotRegistry enotRegistry);
}
