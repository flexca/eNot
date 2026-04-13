package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.element.EnotElement;

import java.util.List;

public interface EnotElementValidator {

    void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext);
}
