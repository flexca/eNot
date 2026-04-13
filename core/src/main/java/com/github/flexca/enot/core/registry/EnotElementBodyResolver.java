package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;

public interface EnotElementBodyResolver {

    Object resolveBody(EnotElement element, EnotContext enotContext);
}
