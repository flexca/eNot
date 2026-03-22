package com.github.flexca.enot.core.struct.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ValueSpecification {

    private final ValueType type;
    private final boolean allowMultipleValues;
}
