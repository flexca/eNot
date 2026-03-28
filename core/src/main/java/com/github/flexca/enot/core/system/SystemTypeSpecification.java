package com.github.flexca.enot.core.system;

import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.system.validation.SystemElementValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SystemTypeSpecification implements EnotTypeSpecification {

    public static final String TYPE_NAME = "system";

    private final SystemElementValidator systemElementValidator = new SystemElementValidator();

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public List<EnotValueType> getValueTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<EnotAttribute> getAttributes() {
        return Arrays.asList(SystemAttribute.values());
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return SystemAttribute.fromJsonString(name);
    }

    @Override
    public EnotElementSpecification getElementSpecification(EnotElement element) {
        Object kindObject = element.getAttributes().get(SystemAttribute.KIND);
        if (kindObject instanceof String kindString) {
            SystemKind kind = SystemKind.fromString(kindString);
            if (kind != null) {
                return kind;
            }
        }
        throw new EnotInvalidConfigurationException("Invalid or missing kind attribute for system element");
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return systemElementValidator;
    }
}
