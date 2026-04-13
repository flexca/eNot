package com.github.flexca.enot.core.testutil;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotElementReferenceResolver;

import java.util.List;

public class TestResourcesReferenceResolver implements EnotElementReferenceResolver {

    public static final String TYPE = "test_resources";

    @Override
    public String getReferenceType() {
        return TYPE;
    }

    @Override
    public List<EnotElement> resolve(String referenceIdentifier, EnotContext enotContext) {
        try {
            String json = ResourceReaderTestUtils.readResourceFileAsString(referenceIdentifier);
            return enotContext.getEnotParser().parse(json, enotContext);
        } catch(Exception e) {
            throw new EnotInvalidArgumentException("cannot resolve eNot element with identifier: " + referenceIdentifier
                    + ", reson: " + e.getMessage(), e);
        }
    }
}
