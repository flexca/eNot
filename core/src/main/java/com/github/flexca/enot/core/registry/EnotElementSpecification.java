package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;

import java.util.Set;

public interface EnotElementSpecification {

    ValueSpecification getConsumeType();

    ValueSpecification getProduceType();

    Set<EnotAttribute> getRequiredAttributes();

    Set<EnotAttribute> getAllowedAttributes();
}
