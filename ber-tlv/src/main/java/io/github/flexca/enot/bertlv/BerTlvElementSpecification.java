package io.github.flexca.enot.bertlv;

import io.github.flexca.enot.bertlv.attribute.BerTlvAttribute;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import io.github.flexca.enot.core.registry.EnotElementBodyResolver;
import io.github.flexca.enot.core.registry.EnotElementSpecification;

import java.util.Set;

/**
 * Defines the structural contract for {@code ber-tlv} elements in eNot templates.
 * <p>
 * This specification is consulted by the eNot parser to validate element structure:
 * <ul>
 *   <li><b>Consumed type:</b> {@code BINARY} (the element body must resolve to binary data
 *       or nested {@code ber-tlv} elements)</li>
 *   <li><b>Produced type:</b> {@code BINARY} (serialization outputs binary bytes)</li>
 *   <li><b>Required attributes:</b> {@link BerTlvAttribute#TAG}</li>
 *   <li><b>Allowed attributes:</b> {@link BerTlvAttribute#TAG}, {@link BerTlvAttribute#MIN_LENGTH},
 *       {@link BerTlvAttribute#MAX_LENGTH}, {@link BerTlvAttribute#INDEFINITE_FORM}</li>
 * </ul>
 * A single shared instance is created by {@link BerTlvEnotTypeSpecification} and reused for
 * all {@code ber-tlv} elements.
 */
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
