package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;

public interface EnotTypeSpecification {

    String getTypeName();

    EnotAttribute resolveAttributeByName(String name);

    EnotElementSpecification getElementSpecification(EnotElement element);

    EnotElementValidator getElementValidator();
}
