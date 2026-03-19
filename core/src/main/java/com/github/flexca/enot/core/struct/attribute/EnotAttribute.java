package com.github.flexca.enot.core.struct.attribute;

import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;

public interface EnotAttribute {

    String getName();

    ValueType getValueType();
}
