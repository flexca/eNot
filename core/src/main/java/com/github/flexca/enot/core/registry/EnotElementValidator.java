package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.parser.JsonError;
import com.github.flexca.enot.core.struct.EnotElement;

import java.util.List;

public interface EnotElementValidator {

    void validateElement(EnotElement element, String parentPath, List<JsonError> jsonErrors);
}
