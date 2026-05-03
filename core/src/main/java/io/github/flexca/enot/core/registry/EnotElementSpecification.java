package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;

import java.util.Set;

/**
 * Describes the structural contract of a single eNot element within the generic
 * validation pipeline.
 *
 * <p>The specification covers four aspects:
 * <ul>
 *   <li><b>consume type</b> — the {@link io.github.flexca.enot.core.element.value.EnotValueType}
 *       of the element body this element accepts as input.</li>
 *   <li><b>produce type</b> — the value type this element outputs after serialization,
 *       used to validate compatibility with the parent element's consume type.</li>
 *   <li><b>required attributes</b> — attributes that must be present; absence causes
 *       an {@link io.github.flexca.enot.core.exception.EnotParsingException}.</li>
 *   <li><b>allowed attributes</b> — the full set of permitted attributes; presence of
 *       any other attribute causes an {@link io.github.flexca.enot.core.exception.EnotParsingException}.</li>
 * </ul>
 *
 * <p>When {@link EnotTypeSpecification#getElementSpecification(io.github.flexca.enot.core.element.EnotElement)}
 * returns {@code null}, the generic validation step is skipped entirely and validation
 * is left to the type-specific {@link EnotElementValidator}.
 */
public interface EnotElementSpecification {

    /**
     * Provide information about type of eNot element body it can consume. Required for validation to check that parent eNot
     * element can consume provided in JSON body during parsing and serialization.
     * @return ValueSpecification with EnotValueType and boolean flag allowMultipleValues, must not return null,
     * use CommonValueType.EMPY when element don't consume at all
     */
    EnotValueSpecification getConsumeType();

    /**
     * Provide information about type of data eNot element produce after serialization. Required for validation to check that parent eNot
     * element can consume provided in JSON body during parsing and serialization.
     * @return ValueSpecification with EnotValueType and boolean flag allowMultipleValues, must not return null.
     */
    EnotValueSpecification getProduceType();

    /**
     * Provide set of required attributes, if during validation all this attributes is not present in eNot element then
     * EnotParsingException will be thrown
     * @return Set of required attribute, can return null or empty Set, in both cases no generic validation will be performed
     */
    Set<EnotAttribute> getRequiredAttributes();

    /**
     * Provide set of allowed attributes, if during validation at least one of eNot element attributes is not in this Set -
     * then EnotParsingException will be thrown
     * @return Set of allowed attribute, can return null or empty Set, in both cases no generic validation will be performed
     */
    Set<EnotAttribute> getAllowedAttributes();

    /**
     * Returns the {@link EnotElementBodyResolver} for this element, or {@code null}
     * if the element's body is provided inline in the template and requires no
     * external resolution.
     *
     * <p>A non-{@code null} resolver is called by the parser to fetch or compute
     * the element body at parse time (e.g. for {@code system/reference} elements
     * that load another template).
     *
     * @return the body resolver, or {@code null} for inline elements
     */
    EnotElementBodyResolver getBodyResolver();
}
