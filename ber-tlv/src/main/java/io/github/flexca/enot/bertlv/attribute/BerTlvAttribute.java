package io.github.flexca.enot.bertlv.attribute;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the attributes available on a {@code ber-tlv} element in an eNot template.
 * <p>
 * These attributes map directly to fields in the element's {@code attributes} block
 * in JSON or YAML templates. The only required attribute is {@link #TAG}; all others are optional.
 */
public enum BerTlvAttribute implements EnotAttribute {

    /**
     * The BER tag that identifies the data object.
     * <p>
     * Value must be a hex-encoded string of 1–4 bytes, e.g. {@code "04"} or {@code "9F1A"}.
     * The tag structure must comply with ITU-T X.690 (validated by {@link io.github.flexca.enot.bertlv.util.BerTlvUtils#isValidTagLength}).
     * <p>
     * This attribute is <b>required</b>.
     */
    TAG("tag", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),

    /**
     * Minimum allowed byte length of the value field (inclusive).
     * <p>
     * Must be a non-negative integer. If both {@code min_length} and {@code max_length} are specified,
     * {@code min_length} must be ≤ {@code max_length}.
     */
    MIN_LENGTH("min_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),

    /**
     * Maximum allowed byte length of the value field (inclusive).
     * <p>
     * Must be a non-negative integer. If both {@code min_length} and {@code max_length} are specified,
     * {@code max_length} must be ≥ {@code min_length}.
     */
    MAX_LENGTH("max_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),

    /**
     * Whether to use indefinite-form length encoding.
     * <p>
     * If {@code true}, the length field is encoded as {@code 0x80} and the encoded value
     * is followed by a two-byte end-of-contents terminator ({@code 00 00}).
     * Must be a boolean. Defaults to {@code false}.
     */
    INDEFINITE_FORM("indefinite_form", new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false));

    private static final Map<String, BerTlvAttribute> BY_NAME = new HashMap<>();
    static {
        for(BerTlvAttribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification valueSpecification;

    private BerTlvAttribute(String name, EnotValueSpecification valueSpecification) {
        this.name = name;
        this.valueSpecification = valueSpecification;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return valueSpecification;
    }

    /**
     * Returns the {@link BerTlvAttribute} with the given name, or {@code null} if not found.
     * The lookup is case-insensitive.
     *
     * @param name the attribute name as it appears in the template
     * @return the matching attribute, or {@code null}
     */
    public static EnotAttribute getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
