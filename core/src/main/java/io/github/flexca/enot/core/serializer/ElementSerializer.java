package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.List;

/**
 * SPI for converting a parsed {@link EnotElement} into encoded output fragments.
 *
 * <p>Each {@link EnotTypeSpecification} provides
 * one or more implementations that know how to turn a specific element kind (ASN.1 sequence,
 * system bit_map, etc.) into a list of {@link ElementSerializationResult} objects.
 * The results are collected by the parent serializer and assembled into the final byte
 * representation.</p>
 *
 * <p>Implementations are typically obtained via
 * {@link EnotTypeSpecification#getSerializer(EnotElement)}
 * and are not required to be thread-safe; a new instance may be created per call.</p>
 */
public interface ElementSerializer {

    /**
     * Serializes {@code element} and returns zero or more encoded fragments.
     *
     * <p>Returning an empty list is valid for optional elements whose value is absent
     * in the current {@code context}. Returning multiple results is used by container
     * elements (e.g. LOOP) that expand a single template element into a sequence of
     * encoded values.</p>
     *
     * @param element     the parsed element to serialize
     * @param context     the active serialization context, used to resolve
     *                    {@code ${placeholder}} references in the element body
     * @param jsonPath    JSON Pointer prefix for the current position in the template,
     *                    appended to error messages to pinpoint the failing node
     * @param enotContext the registry and shared configuration for this serialization run
     * @return the encoded fragments produced by this element; never {@code null}
     * @throws EnotSerializationException if the element cannot be serialized due to a
     *                                    missing required value, type mismatch, or
     *                                    encoding error
     */
    List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                               EnotContext enotContext) throws EnotSerializationException;
}
