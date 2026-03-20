package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.type.EnotElementType;

public class Asn1TypeSpecification implements EnotTypeSpecification {

    @Override
    public EnotElementType getType() {
        return new Asn1ElementType();
    }
}
