package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class EnotRegistry {

    private final Map<String, EnotTypeSpecification> typeSpecifications = new HashMap<>();
    private final Map<String, EnotValueType> valueTypes = new HashMap<>();

    public EnotRegistry(EnotTypeSpecification... specifications) {
        this(Arrays.asList(specifications));
    }

    public EnotRegistry(Collection<EnotTypeSpecification> specifications) {

        for(EnotValueType commonValueType : CommonEnotValueType.values()) {
            if (commonValueType.haveCyclicDependency()) {
                throw new EnotInvalidConfigurationException("EnotValueType " + commonValueType.getName() + " have cyclic dependency");
            }
            valueTypes.put(commonValueType.getName(), commonValueType);
        }

        for(EnotTypeSpecification specification : specifications) {
            if (StringUtils.isBlank(specification.getTypeName())) {
                throw new EnotInvalidConfigurationException("Type name is blank for " + specification.getClass().getName());
            }
            if (CollectionUtils.isNotEmpty(specification.getValueTypes())) {
                for (EnotValueType customValueType : specification.getValueTypes()) {
                    if (customValueType.haveCyclicDependency()) {
                        throw new EnotInvalidConfigurationException("Custom EnotValueType " + customValueType.getName()
                                + " have cyclic dependency");
                    }
                    if (valueTypes.containsKey(customValueType.getName())) {
                        throw new EnotInvalidConfigurationException("Custom EnotValueType " + customValueType.getName()
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

    public static class Builder {

        private List<EnotTypeSpecification> specifications = new ArrayList<>();

        public Builder() {
        }

        public Builder withTypeSpecification(EnotTypeSpecification typeSpecification) {
            specifications.add(typeSpecification);
            return this;
        }

        public Builder withTypeSpecifications(EnotTypeSpecification... typeSpecifications) {
            Collections.addAll(specifications, typeSpecifications);
            return this;
        }

        public Builder withTypeSpecifications(Collection<EnotTypeSpecification> typeSpecifications) {
            specifications.addAll(typeSpecifications);
            return this;
        }

        public EnotRegistry build() {
            return new EnotRegistry(specifications);
        }
    }
}
