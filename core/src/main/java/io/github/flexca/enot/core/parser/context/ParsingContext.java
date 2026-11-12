package io.github.flexca.enot.core.parser.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParsingContext {

    private final Set<String> compositeIdentifiers;
    private final Map<String, Object> customParams;

    public ParsingContext(Map<String, Object> customParams) {
        compositeIdentifiers = new HashSet<>();
        this.customParams = customParams;
    }

    private ParsingContext(Set<String> compositeIdentifiers, Map<String, Object> customParams) {
        this.compositeIdentifiers = new HashSet<>(compositeIdentifiers);
        this.customParams = customParams;
    }

    public boolean addCompositeIdentifier(String identifier) {
        return compositeIdentifiers.add(identifier);
    }

    public Map<String, Object> getCustomParams() {
        return customParams == null ? Collections.emptyMap() : customParams;
    }

    public ParsingContext copy() {
        ParsingContext parsingContext = new ParsingContext(compositeIdentifiers, customParams);
        return parsingContext;
    }
}
