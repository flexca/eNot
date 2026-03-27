package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnotRegistry {

    private final Map<String, EnotTypeSpecification> typeSpecifications = new HashMap<>();
    private final Map<String, EnotValueType> valueTypes = new HashMap<>();

    public EnotRegistry(EnotTypeSpecification... specifications) {

        for(EnotValueType commonValueType : CommonEnotValueType.values()) {
            valueTypes.put(commonValueType.getName(), commonValueType);
        }

        for(EnotTypeSpecification specification : specifications) {
            if (StringUtils.isBlank(specification.getTypeName())) {
                throw new EnotInvalidConfigurationException("Type name is blank for " + specification.getClass().getName());
            }
            if (CollectionUtils.isNotEmpty(specification.getValueTypes())) {
                for (EnotValueType customValueType : specification.getValueTypes()) {
                    if (valueTypes.containsKey(customValueType.getName())) {
                        throw new EnotInvalidConfigurationException("Custom ValueType " + customValueType.getName()
                                + " provided by eNot elements of type " + specification.getTypeName() + " was already used, change ValueType to unique");
                    }
                }
            }
            typeSpecifications.put(specification.getTypeName().toLowerCase(), specification);
        }


    }

    public Optional<EnotTypeSpecification> getTypeSpecification(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            throw new EnotInvalidArgumentException("Provide not blank typeName");
        }
        EnotTypeSpecification specification = typeSpecifications.get(typeName.toLowerCase());
        return specification == null ? Optional.empty() : Optional.of(specification);
    }

    public Optional<EnotValueType> getValueType(String name) {
        if (StringUtils.isBlank(name)) {
            throw new EnotInvalidArgumentException("Provide not blank ValueType name");
        }
        EnotValueType valueType = valueTypes.get(name);
        return valueType == null ? Optional.empty() : Optional.of(valueType);
    }
}
