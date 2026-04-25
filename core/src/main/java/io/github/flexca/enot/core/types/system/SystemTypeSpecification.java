package io.github.flexca.enot.core.types.system;

import io.github.flexca.enot.core.registry.EnotElementPathAltering;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.serializer.ElementSerializer;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.types.system.validation.SystemElementValidator;

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
        SystemKind kind = getKindByElement(element);
        if (kind == null) {
            return null;
        }
        return kind.getElementSerializer();
    }

    @Override
    public EnotElementPathAltering getPathAltering(EnotElement element) {
        SystemKind kind = getKindByElement(element);
        if (SystemKind.LOOP.equals(kind)) {
            Object itemsName = element.getAttributes().get(SystemAttribute.ITEMS_NAME);
            if (itemsName instanceof String itemsNameStr) {
                return EnotElementPathAltering.arrayScope(itemsNameStr);
            }
        } else if (SystemKind.GROUP.equals(kind)) {
            Object groupName = element.getAttributes().get(SystemAttribute.GROUP_NAME);
            if (groupName instanceof String groupNameStr) {
                return EnotElementPathAltering.mapScope(groupNameStr);
            }
        }
        return EnotElementPathAltering.none();
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
