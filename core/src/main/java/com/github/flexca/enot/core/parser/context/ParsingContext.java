package com.github.flexca.enot.core.parser.context;

import java.util.HashSet;
import java.util.Set;

public class ParsingContext {

    private final Set<String> compositeIdentifiers;

    public ParsingContext() {
        compositeIdentifiers = new HashSet<>();
    }

    private ParsingContext(Set<String> compositeIdentifiers) {
        this.compositeIdentifiers = new HashSet<>(compositeIdentifiers);
    }

    public boolean addCompositeIdentifier(String identifier) {
        return compositeIdentifiers.add(identifier);
    }

    public ParsingContext copy() {
        ParsingContext parsingContext = new ParsingContext(compositeIdentifiers);
        return parsingContext;
    }
}
