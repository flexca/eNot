package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.context.ParsingContext;

import java.util.List;

/**
 * Extension point for resolving {@code system/reference} elements at parse time.
 *
 * <p>When the parser encounters a {@code system/reference} element it looks up the
 * resolver whose {@link #getReferenceType()} matches the element's {@code reference_type}
 * attribute and delegates body resolution to it.
 *
 * <p>Implement this interface to load referenced templates from any source — classpath
 * resources, a database, a REST endpoint, etc. Register the implementation with
 * {@link EnotRegistry.Builder#withElementReferenceResolver}.
 *
 * <p>Example:
 * <pre>{@code
 * public class ClasspathReferenceResolver implements EnotElementReferenceResolver {
 *
 *     public String getReferenceType() { return "classpath"; }
 *
 *     public List<EnotElement> resolve(String id, EnotContext ctx, ParsingContext pCtx) {
 *         String json = loadFromClasspath(id);
 *         return ctx.getEnotParser().parse(json, ctx);
 *     }
 * }
 * }</pre>
 */
public interface EnotElementReferenceResolver {

    /**
     * Returns the reference type key that this resolver handles.
     *
     * <p>This value is matched against the {@code reference_type} attribute of
     * {@code system/reference} elements in templates. Must be unique across all
     * resolvers registered in the same {@link EnotRegistry}.
     *
     * @return a non-null, non-blank string identifying this resolver's type
     */
    String getReferenceType();

    /**
     * Resolves and returns the list of {@link EnotElement} instances that form the
     * body of the referencing element.
     *
     * <p>Implementations may perform I/O or invoke further parsing. The parser handles
     * cycle detection provided the body resolver returns a unique identifier via
     * {@link EnotElementBodyResolver#getUniqueCompositeIdentifier}.
     *
     * @param referenceIdentifier the value of the {@code reference_id} attribute
     *                            from the template element
     * @param enotContext         the current eNot context, providing access to the
     *                            parser, registry, and serializer
     * @param parsingContext      the current parsing context, used for cycle detection
     * @return a non-null list of resolved elements
     */
    List<EnotElement> resolve(String referenceIdentifier, EnotContext enotContext, ParsingContext parsingContext);
}
