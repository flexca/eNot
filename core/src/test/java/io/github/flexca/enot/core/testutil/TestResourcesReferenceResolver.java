package io.github.flexca.enot.core.testutil;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.parser.context.ParsingContext;
import io.github.flexca.enot.core.registry.EnotElementReferenceResolver;

import java.util.List;

public class TestResourcesReferenceResolver implements EnotElementReferenceResolver {

    public static final String TYPE = "test_resources";

    @Override
    public String getReferenceType() {
        return TYPE;
    }

    @Override
    public List<EnotElement> resolve(String referenceIdentifier, EnotContext enotContext, ParsingContext parsingContext) {
        try {
            String json = ResourceReaderTestUtils.readResourceFileAsString(referenceIdentifier);
            return enotContext.getEnotParser().parse(json, enotContext, parsingContext);
        } catch(Exception e) {
            throw new EnotInvalidArgumentException("cannot resolve eNot element with identifier: " + referenceIdentifier
                    + ", reson: " + e.getMessage(), e);
        }
    }
}
