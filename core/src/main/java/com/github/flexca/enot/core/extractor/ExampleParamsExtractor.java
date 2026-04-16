package com.github.flexca.enot.core.extractor;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.serializer.context.ContextNode;

import java.util.Collection;
import java.util.List;

public class ExampleParamsExtractor {

    public ContextNode extractExampleParams(List<EnotElement> elements) {

        return null;
    }

    private ContextNode extractPlaceholdersFromElementBody(EnotElement element, List<String> pathParts) {

        Object objectBody = element.getBody();
        if (objectBody instanceof Collection<?> collectionBody) {

        } else if (objectBody instanceof EnotElement elementBody) {

        } else {

        }
        return null;
    }
}
