package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.element.EnotElement;

import java.util.List;

/**
 * Type-specific validation hook called by the parser after the generic attribute and
 * body validation pass has completed successfully.
 *
 * <p>Implement this interface to enforce additional invariants that are specific to
 * one element type and cannot be expressed purely through the allowed/required
 * attribute sets in {@link EnotElementSpecification}. Register the implementation
 * by returning it from {@link EnotTypeSpecification#getElementValidator()}.
 *
 * <h2>Error reporting</h2>
 * Implementations must <em>not</em> throw exceptions. Instead, append one
 * {@link EnotJsonError} per problem to the provided {@code jsonErrors} list using
 * {@link EnotJsonError#of(String, String)}. The parser collects all errors from
 * every element in the template before throwing a single
 * {@link io.github.flexca.enot.core.exception.EnotParsingException}, so this
 * approach allows the user to see all mistakes at once rather than one at a time.
 *
 * <h2>Path convention</h2>
 * Build the JSON-Pointer path for each error by appending the relevant key to
 * {@code parentPath}, for example:
 * <pre>{@code
 * jsonErrors.add(EnotJsonError.of(
 *         parentPath + "/" + MyAttribute.TAG.getName(),
 *         "tag value must be between 0 and 30"));
 * }</pre>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class MyTagValidator implements EnotElementValidator {
 *
 *     &#64;Override
 *     public void validateElement(EnotElement element, String parentPath,
 *                                 List<EnotJsonError> jsonErrors, EnotContext ctx) {
 *
 *         Object tagValue = element.getAttribute(MyAttribute.TAG);
 *         if (tagValue instanceof Integer tag && (tag < 0 || tag > 30)) {
 *             jsonErrors.add(EnotJsonError.of(
 *                     parentPath + "/" + MyAttribute.TAG.getName(),
 *                     "tag must be in range [0, 30], got: " + tag));
 *         }
 *     }
 * }
 * }</pre>
 */
public interface EnotElementValidator {

    /**
     * Validates the given element and appends any errors found to {@code jsonErrors}.
     *
     * <p>This method is only called when all generic validations (required/allowed
     * attributes, body type) have already passed. Return immediately without adding
     * errors if the element is valid.
     *
     * @param element     the element to validate
     * @param parentPath  JSON-Pointer prefix for the current element (e.g.
     *                    {@code "/0/body/2"}); use it as the base when constructing
     *                    error paths
     * @param jsonErrors  mutable list to which errors should be appended; never
     *                    {@code null}
     * @param enotContext the current eNot context, providing access to the registry
     *                    and expression parser if needed
     */
    void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext);
}
