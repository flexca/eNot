package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import io.github.flexca.enot.core.serializer.ElementSerializer;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnotRegistryTest {

    // -----------------------------------------------------------------------
    // Minimal valid stub implementations used across tests
    // -----------------------------------------------------------------------

    /** A minimal EnotAttribute implementation for use in specs. */
    private static EnotAttribute validAttribute(String name) {
        return new EnotAttribute() {
            @Override public String getName() { return name; }
            @Override public EnotValueSpecification getValueSpecification() {
                return new EnotValueSpecification(CommonEnotValueType.TEXT, false);
            }
        };
    }

    /** A minimal EnotValueType with a no-op binary converter (no cyclic deps). */
    private static EnotValueType validValueType(String name) {
        return new EnotValueType() {
            @Override public String getName() { return name; }
            @Override public Set<EnotValueType> getSuperTypes() { return Collections.emptySet(); }
            @Override public boolean isAllowedForAttributes() { return false; }
            @Override public io.github.flexca.enot.core.registry.EnotBinaryConverter getBinaryConverter() {
                return input -> new byte[0];
            }
        };
    }

    /**
     * Builds a minimal valid EnotTypeSpecification with the given type name,
     * no custom value types, and the supplied attribute list.
     */
    private static EnotTypeSpecification validSpec(String typeName, List<EnotAttribute> attributes) {
        return new EnotTypeSpecification() {
            @Override public String getTypeName() { return typeName; }
            @Override public List<EnotValueType> getValueTypes() { return Collections.emptyList(); }
            @Override public List<EnotAttribute> getAttributes() { return attributes; }
            @Override public EnotAttribute resolveAttributeByName(String name) {
                return attributes.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
            }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };
    }

    private static EnotTypeSpecification validSpec(String typeName) {
        return validSpec(typeName, Collections.emptyList());
    }

    // -----------------------------------------------------------------------
    // Success cases
    // -----------------------------------------------------------------------

    @Test
    void testEmptyRegistryBuildsSuccessfully() {
        EnotRegistry registry = new EnotRegistry.Builder().build();
        assertThat(registry).isNotNull();
    }

    @Test
    void testValidCustomTypeSpecRegisters() {
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.custom-type"))
                .build();

        assertThat(registry.getTypeSpecification("my.custom-type")).isPresent();
    }

    @Test
    void testTypeSpecLookupIsCaseInsensitive() {
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("MyType"))
                .build();

        assertThat(registry.getTypeSpecification("mytype")).isPresent();
        assertThat(registry.getTypeSpecification("MYTYPE")).isPresent();
        assertThat(registry.getTypeSpecification("MyType")).isPresent();
    }

    @Test
    void testGetTypeSpecificationReturnsEmptyForUnknownType() {
        EnotRegistry registry = new EnotRegistry.Builder().build();

        assertThat(registry.getTypeSpecification("nonexistent")).isEmpty();
    }

    @Test
    void testGetValueTypeReturnsEmptyForUnknownName() {
        EnotRegistry registry = new EnotRegistry.Builder().build();

        assertThat(registry.getValueType("nonexistent_value_type")).isEmpty();
    }

    @Test
    void testBuiltInValueTypesAlwaysPresent() {
        EnotRegistry registry = new EnotRegistry.Builder().build();

        assertThat(registry.getValueType("text")).isPresent();
        assertThat(registry.getValueType("binary")).isPresent();
        assertThat(registry.getValueType("boolean")).isPresent();
        assertThat(registry.getValueType("integer")).isPresent();
    }

    @Test
    void testValidCustomValueTypeRegisters() {
        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() {
                return List.of(validValueType("my_custom_binary"));
            }
            @Override public List<EnotAttribute> getAttributes() { return Collections.emptyList(); }
            @Override public EnotAttribute resolveAttributeByName(String name) { return null; }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        EnotRegistry registry = new EnotRegistry.Builder().withTypeSpecification(spec).build();
        assertThat(registry.getValueType("my_custom_binary")).isPresent();
    }

    @Test
    void testValidAttributeOnSpecRegisters() {
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(validAttribute("my_attr"))))
                .build();

        assertThat(registry.getTypeSpecification("my.type")).isPresent();
    }

    @Test
    void testAttributeWithCustomValueTypeContributedBySameSpeсRegisters() {

        // The spec contributes a custom value type, and one of its attributes
        // uses that same custom type in its EnotValueSpecification.
        // Registry build must succeed and the value type must be directly retrievable.
        EnotValueType customType = validValueType("my_enum_type");

        EnotAttribute attrUsingCustomType = new EnotAttribute() {
            @Override public String getName() { return "my_attr"; }
            @Override public EnotValueSpecification getValueSpecification() {
                return new EnotValueSpecification(customType, false);
            }
        };

        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.complex.type"; }
            @Override public List<EnotValueType> getValueTypes() { return List.of(customType); }
            @Override public List<EnotAttribute> getAttributes() { return List.of(attrUsingCustomType); }
            @Override public EnotAttribute resolveAttributeByName(String name) {
                return "my_attr".equals(name) ? attrUsingCustomType : null;
            }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        EnotRegistry registry = new EnotRegistry.Builder().withTypeSpecification(spec).build();

        assertThat(registry.getTypeSpecification("my.complex.type")).isPresent();
        assertThat(registry.getValueType("my_enum_type")).isPresent();
    }

    // -----------------------------------------------------------------------
    // Failure — type name validation
    // -----------------------------------------------------------------------

    @Test
    void testBlankTypeNameThrows() {
        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("   "))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class);
    }

    @Test
    void testTypeNameWithSpaceThrows() {
        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my type"))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class);
    }

    @Test
    void testTypeNameWithAtSignThrows() {
        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my@type"))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class);
    }

    // -----------------------------------------------------------------------
    // Failure — custom value type validation
    // -----------------------------------------------------------------------

    @Test
    void testCustomValueTypeDuplicatesBuiltInNameThrows() {
        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() {
                // "text" is a built-in CommonEnotValueType name
                return List.of(validValueType("text"));
            }
            @Override public List<EnotAttribute> getAttributes() { return Collections.emptyList(); }
            @Override public EnotAttribute resolveAttributeByName(String name) { return null; }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder().withTypeSpecification(spec).build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("was already used");
    }

    @Test
    void testCustomValueTypeWithNullBinaryConverterThrows() {
        EnotValueType noConverter = new EnotValueType() {
            @Override public String getName() { return "my_type_no_converter"; }
            @Override public Set<EnotValueType> getSuperTypes() { return Collections.emptySet(); }
            @Override public boolean isAllowedForAttributes() { return false; }
            @Override public io.github.flexca.enot.core.registry.EnotBinaryConverter getBinaryConverter() { return null; }
        };

        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() { return List.of(noConverter); }
            @Override public List<EnotAttribute> getAttributes() { return Collections.emptyList(); }
            @Override public EnotAttribute resolveAttributeByName(String name) { return null; }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder().withTypeSpecification(spec).build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("getBinaryConverter return null");
    }

    // -----------------------------------------------------------------------
    // Failure — attributes validation
    // -----------------------------------------------------------------------

    @Test
    void testGetAttributesReturnsNullThrows() {
        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() { return Collections.emptyList(); }
            @Override public List<EnotAttribute> getAttributes() { return null; }
            @Override public EnotAttribute resolveAttributeByName(String name) { return null; }
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder().withTypeSpecification(spec).build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("return null");
    }

    @Test
    void testAttributeWithBlankNameThrows() {
        EnotAttribute blankName = new EnotAttribute() {
            @Override public String getName() { return "   "; }
            @Override public EnotValueSpecification getValueSpecification() {
                return new EnotValueSpecification(CommonEnotValueType.TEXT, false);
            }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(blankName)))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("blank attribute name");
    }

    @Test
    void testAttributeNameWithHyphenThrows() {
        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(validAttribute("my-attr"))))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("unsupported chars");
    }

    @Test
    void testAttributeNameWithSpaceThrows() {
        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(validAttribute("my attr"))))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("unsupported chars");
    }

    @Test
    void testResolveAttributeByNameReturnsNullThrows() {
        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() { return Collections.emptyList(); }
            @Override public List<EnotAttribute> getAttributes() { return List.of(validAttribute("my_attr")); }
            @Override public EnotAttribute resolveAttributeByName(String name) { return null; }  // broken
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder().withTypeSpecification(spec).build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("resolveAttributeByName method implemented incorrectly");
    }

    @Test
    void testResolveAttributeByNameReturnsDifferentNameThrows() {
        EnotTypeSpecification spec = new EnotTypeSpecification() {
            @Override public String getTypeName() { return "my.type"; }
            @Override public List<EnotValueType> getValueTypes() { return Collections.emptyList(); }
            @Override public List<EnotAttribute> getAttributes() { return List.of(validAttribute("my_attr")); }
            @Override public EnotAttribute resolveAttributeByName(String name) { return validAttribute("other_attr"); } // wrong name
            @Override public EnotElementSpecification getElementSpecification(EnotElement element) { return null; }
            @Override public EnotElementValidator getElementValidator() { return null; }
            @Override public ElementSerializer getSerializer(EnotElement element) { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder().withTypeSpecification(spec).build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("resolveAttributeByName method implemented incorrectly");
    }

    @Test
    void testAttributeWithNullValueSpecificationThrows() {
        EnotAttribute nullSpec = new EnotAttribute() {
            @Override public String getName() { return "my_attr"; }
            @Override public EnotValueSpecification getValueSpecification() { return null; }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(nullSpec)))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("EnotValueSpecification is missing");
    }

    @Test
    void testAttributeValueSpecificationWithNullTypeThrows() {
        EnotAttribute nullType = new EnotAttribute() {
            @Override public String getName() { return "my_attr"; }
            @Override public EnotValueSpecification getValueSpecification() {
                return new EnotValueSpecification(null, false);
            }
        };

        assertThatThrownBy(() -> new EnotRegistry.Builder()
                .withTypeSpecification(validSpec("my.type", List.of(nullType)))
                .build())
                .isInstanceOf(EnotInvalidConfigurationException.class)
                .hasMessageContaining("getType() must not return null");
    }

    // -----------------------------------------------------------------------
    // Failure — lookup argument guard
    // -----------------------------------------------------------------------

    @Test
    void testGetTypeSpecificationWithBlankNameThrows() {
        EnotRegistry registry = new EnotRegistry.Builder().build();

        assertThatThrownBy(() -> registry.getTypeSpecification("   "))
                .isInstanceOf(EnotInvalidArgumentException.class);

        assertThatThrownBy(() -> registry.getTypeSpecification(null))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void testGetValueTypeWithBlankNameThrows() {
        EnotRegistry registry = new EnotRegistry.Builder().build();

        assertThatThrownBy(() -> registry.getValueType("   "))
                .isInstanceOf(EnotInvalidArgumentException.class);

        assertThatThrownBy(() -> registry.getValueType(null))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }
}
