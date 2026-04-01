package com.github.flexca.enot.core.types.system;

import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.EnotValueType;
import com.github.flexca.enot.core.serializer.ElementSerializer;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.types.system.validation.SystemElementValidator;

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
        return getKindByElement(element);
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return systemElementValidator;
    }

    @Override
    public ElementSerializer getSerializer(EnotElement element) {
        return null;
    }

    private SystemKind getKindByElement(EnotElement element) {
        Object kindObject = element.getAttributes().get(SystemAttribute.KIND);
        if (kindObject instanceof String kindString) {
            SystemKind kind = SystemKind.fromString(kindString);
            if (kind != null) {
                return kind;
            }
        }
        return null;
    }
}
