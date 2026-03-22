package com.github.flexca.enot.core.system;

import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.system.validation.SystemElementValidator;

public class SystemTypeSpecification implements EnotTypeSpecification {

    public static final String TYPE_NAME = "system";

    private final SystemElementValidator systemElementValidator = new SystemElementValidator();

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return SystemAttribute.fromJsonString(name);
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return systemElementValidator;
    }
}
