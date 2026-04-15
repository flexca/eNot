package com.github.flexca.enot.core.types.system;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotElementBodyResolver;
import com.github.flexca.enot.core.registry.EnotElementReferenceResolver;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

/**
 * {@link EnotElementBodyResolver} for {@code system/reference} elements.
 *
 * <p>Resolves the body by delegating to the {@link com.github.flexca.enot.core.registry.EnotElementReferenceResolver}
 * registered in the {@link com.github.flexca.enot.core.registry.EnotRegistry} for the element's
 * {@code reference_type} attribute. The resolver is looked up at parse time, so all reference
 * types must be registered before parsing begins.
 *
 * <p>The composite identifier returned by {@link #getUniqueCompositeIdentifier} is
 * {@code "<reference_type>:<reference_identifier>"}, which makes cycles across different
 * reference type implementations distinguishable.
 */
public class SystemReferenceBodyResolver implements EnotElementBodyResolver {

    @Override
    public String getUniqueCompositeIdentifier(EnotElement element) {

        String referenceType = getReferenceType(element);
        String referenceIdentifier = getReferenceIdentifier(element);
        return referenceType + ":" + referenceIdentifier;
    }

    @Override
    public Object resolveBody(EnotElement element, EnotContext enotContext) {

        String referenceType = getReferenceType(element);
        EnotElementReferenceResolver referenceResolver = enotContext.getEnotRegistry().getElementReferenceResolver(referenceType);
        if(referenceResolver == null) {
            throw new EnotInvalidArgumentException("no registered EnotElementReferenceResolver found for reference type: "
                    + referenceType);
        }
        String referenceIdentifier = getReferenceIdentifier(element);
        return referenceResolver.resolve(referenceIdentifier, enotContext);
    }

    private String getReferenceType(EnotElement element) {

        Object referenceType = element.getAttribute(SystemAttribute.REFERENCE_TYPE);
        if (referenceType == null) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_TYPE.getName()
                    + " must be set for system element reference");
        }
        if (!(referenceType instanceof String)) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_TYPE.getName() + " must be string");
        }
        return (String) referenceType;
    }

    private String getReferenceIdentifier(EnotElement element) {

        Object referenceIdentifier = element.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER);
        if (referenceIdentifier == null) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName()
                    + " must be set for system element reference");
        }
        if (!(referenceIdentifier instanceof String)) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName()
                    + " must be string");
        }
        return (String) referenceIdentifier;
    }
}
