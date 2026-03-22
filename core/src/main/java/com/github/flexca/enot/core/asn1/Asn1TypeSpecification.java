package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.asn1.validation.Asn1ElementValidator;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;

public class Asn1TypeSpecification implements EnotTypeSpecification {

    public static final String TYPE_NAME = "asn.1";

    private final Asn1ElementValidator asn1ElementValidator = new Asn1ElementValidator();

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return Asn1Attribute.fromJsonString(name);
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return asn1ElementValidator;
    }
}
