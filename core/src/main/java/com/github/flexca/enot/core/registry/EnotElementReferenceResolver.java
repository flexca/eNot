package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.parser.context.ParsingContext;

import java.util.List;

public interface EnotElementReferenceResolver {

    String getReferenceType();

    List<EnotElement> resolve(String referenceIdentifier, EnotContext enotContext, ParsingContext parsingContext);
}
