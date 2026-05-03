package io.github.flexca.enot.core.registry;

/**
 * Strategy interface for converting a resolved Java value into its binary (byte-array)
 * representation for use in the eNot serialization pipeline.
 *
 * <p>Each {@link io.github.flexca.enot.core.element.value.EnotValueType} carries one
 * {@code EnotBinaryConverter} instance that is called by the serializer after the
 * placeholder or literal value has been resolved from the
 * {@link io.github.flexca.enot.core.serializer.context.SerializationContext}.
 *
 * <h2>Implementing a custom converter</h2>
 * <pre>{@code
 * public class HexStringToBinaryConverter implements EnotBinaryConverter {
 *
 *     &#64;Override
 *     public byte[] toBinary(Object input) {
 *         if (input == null) { return null; }
 *         if (input instanceof String hex) {
 *             return HexFormat.of().parseHex(hex);
 *         }
 *         throw new EnotInvalidArgumentException("expected a hex string, got: " + input.getClass());
 *     }
 * }
 * }</pre>
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>Return {@code null} when {@code input} is {@code null}. The serializer will
 *       treat {@code null} output as an absent value and either skip optional elements
 *       or throw {@link io.github.flexca.enot.core.exception.EnotSerializationException}
 *       for required ones.</li>
 *   <li>Throw {@link io.github.flexca.enot.core.exception.EnotInvalidArgumentException}
 *       when {@code input} is of an unexpected Java type.</li>
 *   <li>Throw {@link io.github.flexca.enot.core.exception.EnotDataConvertingException}
 *       when {@code input} has the right type but contains malformed content (e.g. an
 *       invalid OID string).</li>
 * </ul>
 */
public interface EnotBinaryConverter {

    /**
     * Converts the resolved {@code input} value to its binary representation.
     *
     * @param input the resolved value from the serialization context; may be {@code null}
     *              when an optional placeholder was not provided
     * @return the binary encoding of {@code input}, or {@code null} if {@code input}
     *         is {@code null}
     * @throws io.github.flexca.enot.core.exception.EnotInvalidArgumentException if
     *         {@code input} is a non-null value of an unexpected Java type
     * @throws io.github.flexca.enot.core.exception.EnotDataConvertingException if
     *         {@code input} has the correct type but contains malformed content
     */
    byte[] toBinary(Object input);
}
