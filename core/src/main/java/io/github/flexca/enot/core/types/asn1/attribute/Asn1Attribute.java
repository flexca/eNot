package io.github.flexca.enot.core.types.asn1.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum Asn1Attribute implements EnotAttribute {

    TAG("tag", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    IMPLICIT("implicit", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    EXPLICIT("explicit", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    APPLY_PADDING("apply_padding", new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false)),
    MIN_LENGTH("min_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    MAX_LENGTH("max_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    ALLOWED_VALUES("allowed_values", new EnotValueSpecification(CommonEnotValueType.ANY, true));

    private static final Map<String, Asn1Attribute> BY_NAME = new HashMap<>();
    static {
        for(Asn1Attribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification ValueSpecification;

    private Asn1Attribute(String name, EnotValueSpecification ValueSpecification) {
        this.name = name;
        this.ValueSpecification = ValueSpecification;
    }

    @Override
    public Asn1Attribute fromName(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }

    @Override
    @JsonValue
    public String getName() {
        return this.name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return ValueSpecification;
    }

    @JsonCreator
    public static Asn1Attribute fromJsonString(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }
}
