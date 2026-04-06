package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.element.EnotElement;

public interface EnotElementReferenceResolver {

    String getResolverName();

    EnotElement resolve(String reference);
}
