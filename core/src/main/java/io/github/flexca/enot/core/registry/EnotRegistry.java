package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry that holds all {@link EnotTypeSpecification} instances and
 * {@link EnotElementReferenceResolver} implementations for a given eNot setup.
 *
 * <p>The registry is constructed once via {@link Builder} and shared across all
 * parse and serialize operations. It is immutable after construction and
 * therefore thread-safe.
 *
 * <p>During construction every registered {@link EnotTypeSpecification} is validated:
 * type names must be non-blank, custom {@link EnotValueType} names must be unique across
 * all specifications, and value-type converters must not be {@code null}. Any violation
 * throws {@link io.github.flexca.enot.core.exception.EnotInvalidConfigurationException}
 * immediately, so misconfiguration is detected at startup rather than at serialization time.
 *
 * <p>Typical usage:
 * <pre>{@code
 * EnotRegistry registry = new EnotRegistry.Builder()
 *         .withTypeSpecifications(new Asn1TypeSpecification(), new SystemTypeSpecification())
 *         .withElementReferenceResolver(new MyFileReferenceResolver())
 *         .build();
 * }</pre>
 */
public class EnotRegistry {

    private final Map<String, EnotTypeSpecification> typeSpecifications = new HashMap<>();
    private final Map<String, EnotValueType> valueTypes = new HashMap<>();
    private final Map<String, EnotElementReferenceResolver> elementReferenceResolvers = new HashMap<>();

    private EnotRegistry(Collection<EnotTypeSpecification> specifications,
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

    /**
     * Returns the {@link EnotTypeSpecification} registered under the given type name.
     *
     * <p>The lookup is case-insensitive.
     *
     * @param typeName the element type name to look up; must not be blank
     * @return an {@link Optional} containing the specification, or empty if none is registered
     * @throws io.github.flexca.enot.core.exception.EnotInvalidArgumentException if {@code typeName} is blank
     */
    public Optional<EnotTypeSpecification> getTypeSpecification(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            throw new EnotInvalidArgumentException("Provide not blank typeName");
        }
        EnotTypeSpecification specification = typeSpecifications.get(typeName.toLowerCase());
        return specification == null ? Optional.empty() : Optional.of(specification);
    }

    /**
     * Returns the {@link EnotValueType} registered under the given name.
     *
     * <p>Built-in value types from {@link io.github.flexca.enot.core.element.value.CommonEnotValueType}
     * are always present. Additional types may be contributed by registered
     * {@link EnotTypeSpecification} instances.
     *
     * @param name the value type name to look up; must not be blank
     * @return an {@link Optional} containing the value type, or empty if none is registered
     * @throws io.github.flexca.enot.core.exception.EnotInvalidArgumentException if {@code name} is blank
     */
    public Optional<EnotValueType> getValueType(String name) {
        if (StringUtils.isBlank(name)) {
            throw new EnotInvalidArgumentException("Provide not blank ValueType name");
        }
        EnotValueType valueType = valueTypes.get(name);
        return valueType == null ? Optional.empty() : Optional.of(valueType);
    }

    /**
     * Returns the {@link EnotElementReferenceResolver} registered for the given reference type,
     * or {@code null} if no resolver has been registered for that type.
     *
     * @param resolverType the reference type key as returned by
     *                     {@link EnotElementReferenceResolver#getReferenceType()}
     * @return the resolver, or {@code null} if not registered
     */
    public EnotElementReferenceResolver getElementReferenceResolver(String resolverType) {

        return elementReferenceResolvers.get(resolverType);
    }

    /**
     * Builder for {@link EnotRegistry}.
     *
     * <p>Register all required {@link EnotTypeSpecification} instances and any
     * {@link EnotElementReferenceResolver} implementations, then call
     * {@link #build()} to obtain an immutable, validated registry.
     */
    public static class Builder {

        private final List<EnotTypeSpecification> specifications = new ArrayList<>();
        private final List<EnotElementReferenceResolver> elementReferenceResolvers = new ArrayList<>();

        public Builder() {
        }

        /**
         * Adds a single {@link EnotTypeSpecification} to the registry.
         *
         * @param typeSpecification the specification to register
         * @return this builder
         */
        public Builder withTypeSpecification(EnotTypeSpecification typeSpecification) {
            specifications.add(typeSpecification);
            return this;
        }

        /**
         * Adds multiple {@link EnotTypeSpecification} instances to the registry (varargs overload).
         *
         * @param typeSpecifications the specifications to register
         * @return this builder
         */
        public Builder withTypeSpecifications(EnotTypeSpecification... typeSpecifications) {
            Collections.addAll(specifications, typeSpecifications);
            return this;
        }

        /**
         * Adds a collection of {@link EnotTypeSpecification} instances to the registry.
         *
         * @param typeSpecifications the specifications to register
         * @return this builder
         */
        public Builder withTypeSpecifications(Collection<EnotTypeSpecification> typeSpecifications) {
            specifications.addAll(typeSpecifications);
            return this;
        }

        /**
         * Registers an {@link EnotElementReferenceResolver} for resolving
         * {@code system/reference} elements whose {@code reference_type} attribute
         * matches {@link EnotElementReferenceResolver#getReferenceType()}.
         *
         * @param elementReferenceResolver the resolver to register
         * @return this builder
         */
        public Builder withElementReferenceResolver(EnotElementReferenceResolver elementReferenceResolver) {
            elementReferenceResolvers.add(elementReferenceResolver);
            return this;
        }

        /**
         * Builds and returns the {@link EnotRegistry}, validating all registered
         * specifications in the process.
         *
         * @return a new, immutable {@link EnotRegistry}
         * @throws io.github.flexca.enot.core.exception.EnotInvalidConfigurationException if any
         *         registered specification has a blank type name, a duplicate value-type name, or
         *         a value-type converter that returns {@code null}
         */
        public EnotRegistry build() {
            return new EnotRegistry(specifications, elementReferenceResolvers);
        }
    }
}
