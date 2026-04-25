package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.context.ParsingContext;
import io.github.flexca.enot.core.types.system.SystemKind;

/**
 * Strategy interface for resolving an element's body dynamically at parse time.
 *
 * <p>Certain element types — most notably {@code system/reference} — do not carry
 * their body inline in the template. Instead, the body must be fetched or computed
 * when the element is first encountered during parsing. Implementations of this
 * interface encapsulate that resolution logic.
 *
 * <p>The interface also participates in <b>cyclic-dependency detection</b>. Because
 * body resolution may itself trigger further parsing (e.g. a reference that includes
 * another template which in turn references the first), the parser tracks a set of
 * identifiers that are currently being resolved. Implementations that can participate
 * in such cycles must return a non-{@code null} identifier from
 * {@link #getUniqueCompositeIdentifier}; the parser uses this value to detect and
 * report cycles before a {@link StackOverflowError} can occur.
 */
public interface EnotElementBodyResolver {

    /**
     * Returns a unique identifier for the given element that the parser uses to
     * detect cyclic dependencies during resolution.
     *
     * <p>The returned string must uniquely identify the specific external resource
     * or body this element resolves to. For {@code system/reference} elements this
     * is typically a composite of the reference type and identifier, for example
     * {@code "test_resources:json/asn1/rfc/san/san-dns.json"}.
     *
     * <p>If cycle detection is not applicable for this resolver implementation,
     * return {@code null}. The parser will skip cycle detection for that element.
     *
     * @param element the element whose resolution identity is needed
     * @return a non-empty string that uniquely identifies the resolution target,
     *         or {@code null} if cycle detection is not required
     */
    String getUniqueCompositeIdentifier(EnotElement element);

    /**
     * Resolves and returns the body for the given element at parse time.
     *
     * <p>This method is called by the parser when it encounters an element whose
     * {@link SystemKind} has a registered
     * body resolver. The returned value replaces the element's inline body and is
     * stored directly on the parsed {@link EnotElement}.
     *
     * <p>Implementations may perform I/O, invoke further parsing via
     * {@link EnotContext#getEnotParser()}, or compute the body from the element's
     * attributes. When further parsing is involved, cyclic-dependency detection via
     * {@link #getUniqueCompositeIdentifier} should be implemented to prevent
     * infinite recursion.
     *
     * @param element      the element whose body is to be resolved
     * @param enotContext  the current parsing context, providing access to the
     *                     registry, parser, serializer, and expression engine
     * @return the resolved body; the concrete type depends on the element kind
     *         (e.g. {@code List<EnotElement>} for {@code system/reference})
     */
    Object resolveBody(EnotElement element, EnotContext enotContext, ParsingContext parsingContext);
}
