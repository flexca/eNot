package io.github.flexca.enot.bertlv;

import io.github.flexca.enot.bertlv.attribute.BerTlvAttribute;
import io.github.flexca.enot.bertlv.serializer.BerTlvSerializer;
import io.github.flexca.enot.bertlv.validator.BerTlvElementValidator;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.serializer.ElementSerializer;

import java.util.Arrays;
import java.util.List;

/**
 * Type specification for the {@code ber-tlv} element type in the eNot framework.
 * <p>
 * Register this class with {@link io.github.flexca.enot.core.registry.EnotRegistry.Builder}
 * to enable BER-TLV template support:
 * <pre>{@code
 * EnotRegistry registry = new EnotRegistry.Builder()
 *         .withTypeSpecifications(
 *                 new SystemTypeSpecification(),
 *                 new BerTlvEnotTypeSpecification()
 *         )
 *         .build();
 * }</pre>
 * The constructor accepts an optional list of hex tag strings that should bypass
 * ITU-T X.690 structural validation. This is useful for proprietary or non-standard
 * tags used in some smart-card specifications.
 */
public class BerTlvEnotTypeSpecification implements EnotTypeSpecification {

    /** The type name used in templates: {@code "ber-tlv"}. */
    public static final String TYPE = "ber-tlv";

    private static final BerTlvElementSpecification ELEMENT_SPECIFICATION = new BerTlvElementSpecification();
    private static final BerTlvSerializer ELEMENT_SERIALIZER = new BerTlvSerializer();

    private final BerTlvElementValidator elementValidator;

    /**
     * Creates a new type specification.
     *
     * @param tagsToIgnore zero or more hex tag strings (e.g. {@code "1F"}) whose structural
     *                     validity should not be checked by {@link BerTlvElementValidator}
     */
    public BerTlvEnotTypeSpecification(String ... tagsToIgnore) {
        elementValidator = new BerTlvElementValidator(tagsToIgnore);
    }

    @Override
    public String getTypeName() {
        return TYPE;
    }

    @Override
    public List<EnotValueType> getValueTypes() {
        return Arrays.asList(BerTlvValueType.values());
    }

    @Override
    public List<EnotAttribute> getAttributes() {
        return Arrays.asList(BerTlvAttribute.values());
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return BerTlvAttribute.getByName(name);
    }

    @Override
    public EnotElementSpecification getElementSpecification(EnotElement element) {
        return ELEMENT_SPECIFICATION;
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return elementValidator;
    }

    @Override
    public ElementSerializer getSerializer(EnotElement element) {
        return ELEMENT_SERIALIZER;
    }
}
