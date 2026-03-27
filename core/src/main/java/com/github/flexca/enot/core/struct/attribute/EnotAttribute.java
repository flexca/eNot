package com.github.flexca.enot.core.struct.attribute;

import com.github.flexca.enot.core.struct.value.EnotValueType;

public interface EnotAttribute {

    String getName();

    EnotValueType getValueType();

    EnotAttribute fromName(String name);
}
