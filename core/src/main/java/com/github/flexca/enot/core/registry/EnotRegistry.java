package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnotRegistry {

    private final Map<String, EnotTypeSpecification> typeSpecifications = new HashMap<>();


    public EnotRegistry(EnotTypeSpecification... specifications) {
        for(EnotTypeSpecification specification : specifications) {
            if (StringUtils.isBlank(specification.getTypeName())) {
                throw new EnotInvalidConfigurationException("Type name is blank for " + specification.getClass().getName());
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
}
