package com.github.flexca.enot.bertlv;

import com.github.flexca.enot.bertlv.attribute.BerTlvAttribute;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.element.value.EnotValueSpecification;
import com.github.flexca.enot.core.registry.EnotElementBodyResolver;
import com.github.flexca.enot.core.registry.EnotElementSpecification;

import java.util.Set;

public class BerTlvElementSpecification implements EnotElementSpecification {

    private static final EnotValueSpecification CONSUME_TYPE = new EnotValueSpecification(CommonEnotValueType.BINARY,
            true);
    private static final EnotValueSpecification PRODUCE_TYPE = new EnotValueSpecification(CommonEnotValueType.BINARY,
            false);

    private static final Set<EnotAttribute> REQUIRED_ATTRIBUTES = Set.of(BerTlvAttribute.TAG);
    private static final Set<EnotAttribute> ALLOWED_ATTRIBUTES = Set.of(BerTlvAttribute.TAG, BerTlvAttribute.MIN_LENGTH,
            BerTlvAttribute.MAX_LENGTH, BerTlvAttribute.INDEFINITE_FORM);

    @Override
    public EnotValueSpecification getConsumeType() {
        return CONSUME_TYPE;
    }

    @Override
    public EnotValueSpecification getProduceType() {
        return PRODUCE_TYPE;
    }

    @Override
    public Set<EnotAttribute> getRequiredAttributes() {
        return REQUIRED_ATTRIBUTES;
    }

    @Override
    public Set<EnotAttribute> getAllowedAttributes() {
        return ALLOWED_ATTRIBUTES;
    }

    @Override
    public EnotElementBodyResolver getBodyResolver() {
        return null;
    }
}
