package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.context.ParsingContext;

import java.util.List;

public interface EnotElementReferenceResolver {

    String getReferenceType();

    List<EnotElement> resolve(String referenceIdentifier, EnotContext enotContext, ParsingContext parsingContext);
}
