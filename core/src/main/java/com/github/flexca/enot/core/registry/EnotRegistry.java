package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.element.value.EnotValueType;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnotRegistry {

    private final Map<String, EnotTypeSpecification> typeSpecifications = new HashMap<>();
    private final Map<String, EnotValueType> valueTypes = new HashMap<>();
    private final Map<String, EnotSystemVariableProvider> systemVariableProviders = new HashMap<>();
    private final Map<String, EnotElementReferenceResolver> elementReferenceResolvers = new HashMap<>();

    private EnotRegistry(Collection<EnotTypeSpecification> specifications,
                         Collection<EnotSystemVariableProvider> systemVariableProviders,
                         Collection<EnotElementReferenceResolver> elementReferenceResolvers) {

        for (EnotValueType commonValueType : CommonEnotValueType.values()) {
            if (commonValueType.haveCyclicDependency()) {
                throw new EnotInvalidConfigurationException("EnotValueType " + commonValueType.getName() + " have cyclic dependency");
            }
            valueTypes.put(commonValueType.getName(), commonValueType);
        }

        for (EnotTypeSpecification specification : specifications) {
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
                                + " provided by eNot elements of type " + specification.getTypeName()
                                + " was already used, change ValueType name to unique");
                    }
                    if (customValueType.getBinaryConverter() == null) {
                        throw new EnotInvalidConfigurationException("Custom EnotValueType " + customValueType.getName()
                                + " provided by eNot elements of type " + specification.getTypeName()
                                + " getBinaryConverter return null");
                    }
                }
            }
            typeSpecifications.put(specification.getTypeName().toLowerCase(), specification);
        }

        if (CollectionUtils.isNotEmpty(elementReferenceResolvers)) {
            for (EnotElementReferenceResolver resolver : elementReferenceResolvers) {
                this.elementReferenceResolvers.put(resolver.getReferenceType(), resolver);
            }
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

    public EnotElementReferenceResolver getElementReferenceResolver(String resolverType) {

        return elementReferenceResolvers.get(resolverType);
    }

    public static class Builder {

        private final List<EnotTypeSpecification> specifications = new ArrayList<>();
        private final List<EnotSystemVariableProvider> systemVariableProviders = new ArrayList<>();
        private final List<EnotElementReferenceResolver> elementReferenceResolvers = new ArrayList<>();

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

        public Builder withSystemVariableProvider(EnotSystemVariableProvider systemVariableProvider) {
            systemVariableProviders.add(systemVariableProvider);
            return this;
        }

        public Builder withElementReferenceResolver(EnotElementReferenceResolver elementReferenceResolver) {
            elementReferenceResolvers.add(elementReferenceResolver);
            return this;
        }

        public EnotRegistry build() {
            return new EnotRegistry(specifications, systemVariableProviders, elementReferenceResolvers);
        }
    }
}
