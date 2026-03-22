package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;

public interface EnotTypeSpecification {

    String getTypeName();

    EnotAttribute resolveAttributeByName(String name);

    EnotElementValidator getElementValidator();
}
