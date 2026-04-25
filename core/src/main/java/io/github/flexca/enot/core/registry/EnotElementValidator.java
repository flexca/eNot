package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.element.EnotElement;

import java.util.List;

public interface EnotElementValidator {

    void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext);
}
