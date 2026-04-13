package com.github.flexca.enot.core.types.system;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotElementBodyResolver;
import com.github.flexca.enot.core.registry.EnotElementReferenceResolver;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

public class SystemReferenceBodyResolver implements EnotElementBodyResolver {

    @Override
    public Object resolveBody(EnotElement element, EnotContext enotContext) {

        Object referenceType = element.getAttribute(SystemAttribute.REFERENCE_TYPE);
        if (referenceType == null) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_TYPE.getName()
                    + " must be set for system element reference");
        }
        if (!(referenceType instanceof String)) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_TYPE.getName() + " must be string");
        }

        EnotElementReferenceResolver referenceResolver = enotContext.getEnotRegistry().getElementReferenceResolver((String) referenceType);
        if(referenceResolver == null) {
            throw new EnotInvalidArgumentException("no registered EnotElementReferenceResolver found for reference type: "
                    + referenceType);
        }

        Object referenceIdentifier = element.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER);
        if (referenceIdentifier == null) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName()
                    + " must be set for system element reference");
        }
        if (!(referenceIdentifier instanceof String)) {
            throw new EnotInvalidArgumentException("attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName()
                    + " must be string");
        }

        return referenceResolver.resolve((String) referenceIdentifier, enotContext);
    }
}
