# Adding a New eNot Element Type

eNot is designed as an extensible template engine. In addition to the built-in `system` and `asn.1` element types, you can implement and register your own custom element types. This guide walks through all the steps required to do so.

## Table of Contents

- [Implementing EnotTypeSpecification](#implementing-enottypespecification)
  - [getTypeName](#gettypename)
  - [getValueTypes](#getvaluetypes)
  - [getAttributes](#getattributes)
  - [resolveAttributeByName](#resolveattributebyname)
  - [getElementSpecification](#getelementspecification)
  - [getElementValidator](#getelementvalidator)
  - [getSerializer](#getserializer)
- [Registration and usage](#registration-of-mycustomenottypespecification-in-enotregistry-and-usage-in-yaml-or-json)

---

## Implementing EnotTypeSpecification

Every element type must be registered with `EnotRegistry` by providing an implementation of the `EnotTypeSpecification` interface. For reference, this is how the built-in types are registered:

```java
EnotRegistry enotRegistry = new EnotRegistry.Builder()
        .withTypeSpecifications(new SystemTypeSpecification())
        .withTypeSpecifications(new Asn1TypeSpecification())
        .build();
```

The first step is to create your own class that implements `EnotTypeSpecification`, wiring together the components you will build in the sections below:

```java
package my.custom.type;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.serializer.ElementSerializer;

import java.util.List;

public class MyCustomEnotTypeSpecification implements EnotTypeSpecification {

    public static final String TYPE = "my-custom-type";

    private final List<MyCustomValueType> valueTypes = List.of(new MyCustomValueType());
    private final List<EnotAttribute> attributes = List.of(MyCustomAttribute.values());
    private final MyCustomElementSpecification elementSpecification = new MyCustomElementSpecification();
    private final MyCustomElementValidator elementValidator = new MyCustomElementValidator();
    private final MyCustomElementSerializer elementSerializer = new MyCustomElementSerializer();
    
    @Override
    public String getTypeName() {
        return TYPE;
    }

    @Override
    public List<EnotValueType> getValueTypes() {
        return valueTypes;
    }

    @Override
    public List<EnotAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public EnotAttribute resolveAttributeByName(String name) {
        return MyCustomAttribute.getByName(name);
    }

    @Override
    public EnotElementSpecification getElementSpecification(EnotElement element) {
        return elementSpecification;
    }

    @Override
    public EnotElementValidator getElementValidator() {
        return elementValidator;
    }

    @Override
    public ElementSerializer getSerializer(EnotElement element) {
        return elementSerializer;
    }
}
```

> **Thread safety:** the specification, validator, and serializer instances are created once and reused across all calls. All of these implementations must therefore be **stateless and thread-safe** — do not store any mutable per-request state in fields.

Next, the following sections explain how to implement each method.

### getTypeName

`getTypeName()` must return the unique name of your custom type. This is the value users write in the `type` field of their JSON or YAML templates — e.g. `type: my-custom-type`. If two `EnotTypeSpecification` implementations registered with the same `EnotRegistry` return the same type name, an `EnotInvalidConfigurationException` is thrown at startup.

### getValueTypes

`getValueTypes()` returns the value types introduced by your element type. Several common types — boolean, integer, text, binary, and others — are already defined in `CommonEnotValueType`. Return an empty list if your type does not introduce any new value types and relies entirely on the existing ones.

If you do need a new type, create a class that implements `EnotValueType`. You can also use an enum — see `io.github.flexca.enot.core.element.value.CommonEnotValueType` for a reference example.

For instance, to introduce a `hex_string` type that carries a hex-encoded string and can be converted to binary:

```java

import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.exception.EnotDataConvertingException;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.HexFormat;
import java.util.Set;

public class MyCustomValueType implements EnotValueType {

    private final HexStringBinaryConverter hexStringBinaryConverter = new HexStringBinaryConverter();

    @Override
    public String getName() {
        return "hex_string";
    }

    @Override
    public Set<EnotValueType> getSuperTypes() {
        return Set.of(CommonEnotValueType.BINARY, CommonEnotValueType.TEXT);
    }

    @Override
    public boolean isAllowedForAttributes() {
        return false;
    }

    @Override
    public EnotBinaryConverter getBinaryConverter() {
        return hexStringBinaryConverter;
    }

    public static class HexStringBinaryConverter implements EnotBinaryConverter {

        @Override
        public byte[] toBinary(Object input) {

            if (input instanceof String stringInput) {

                if (StringUtils.isBlank(stringInput)) {
                    return new byte[0];
                }
                try {
                    return HexFormat.of().parseHex(stringInput);
                } catch (Exception e) {
                    throw new EnotDataConvertingException("failure during conversion of hex to binary, reason: " + e.getMessage(), e);
                }
            }

            throw new EnotInvalidArgumentException("expecting string input for hex string type");
        }
    }
}
```

Key points:
- `getName()` — must return a unique type name across all value types in your registry
- `getSuperTypes()` — declares parent types; here `hex_string` is both textual and binary, so both supertypes are listed. The framework uses this to check type compatibility between elements.
- `isAllowedForAttributes()` — controls whether this type can be used as an attribute value (vs. element body only)
- `getBinaryConverter()` — required when `CommonEnotValueType.BINARY` is a supertype; the converter turns a value of this type into `byte[]`
- `HexStringBinaryConverter` — the concrete converter that parses the hex string into bytes

### getAttributes

`getAttributes()` returns all attributes your element type exposes in templates. Define them by implementing the `EnotAttribute` interface. Using an enum (as shown below) is the recommended approach — it requires less boilerplate than a separate class per attribute, but plain classes work equally well.

```java
package my.custom.type;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;

import java.util.HashMap;
import java.util.Map;

public enum MyCustomAttribute implements EnotAttribute {

    TAG("tag", new EnotValueSpecification(CommonEnotValueType.TEXT, false)), // accepts a hex string entered as text; format is enforced by MyCustomElementValidator
    MIN_LENGTH("min_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    MAX_LENGTH("max_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false));

    private static final Map<String, MyCustomAttribute> BY_NAME = new HashMap<>();
    static {
        for(MyCustomAttribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification valueSpecification;

    MyCustomAttribute(String name, EnotValueSpecification valueSpecification) {
        this.name = name;
        this.valueSpecification = valueSpecification;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return valueSpecification;
    }

    public static EnotAttribute getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
```
  
In example above enum is implementing interface EnotAttribute. This approach require less code writing comparing to implementing 
same interface for three attributes with three separate classes. But it is just an optimization - no restriction using classes for 
eNot attributes implementation.

Three methods must be implemented:
- `getName()` — the attribute name as it appears in the template's `attributes` block. Names only need to be unique within this element type — different element types may share attribute names.
- `getValueSpecification()` — declares the value type and whether multiple values are allowed for this attribute. **Only value types where `isAllowedForAttributes()` returns `true` may be used here.** `MyCustomValueType` explicitly returns `false` (it is intended for element bodies only), so even though the `tag` attribute accepts a hex-encoded string, its declared type is `CommonEnotValueType.TEXT` — a built-in type that is allowed for attributes. The hex-string format constraint is then enforced in `MyCustomElementValidator`.

### resolveAttributeByName

`resolveAttributeByName(String name)` is the single-attribute lookup counterpart to `getAttributes()`. Given an attribute name from the template, return the matching `EnotAttribute`, or `null` if no attribute with that name exists.

### getElementSpecification

`getElementSpecification(EnotElement element)` returns an `EnotElementSpecification` that tells the parser what a valid element of your type looks like. It defines three things:

- **Consume type** — the value type that the element's body must resolve to. The second constructor argument (`true`/`false`) controls whether multiple body values are allowed.
- **Produce type** — the value type this element outputs after serialization. Parent elements use this to check that they can consume it.
- **Required and allowed attributes** — the parser enforces that required attributes are present and no unlisted attributes appear.

`getBodyResolver()` returns `null` here, which means standard body resolution applies. You only need to return a non-null resolver for advanced cases where you want to control how body content is looked up or referenced.

```java
package my.custom.type;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import io.github.flexca.enot.core.registry.EnotElementBodyResolver;
import io.github.flexca.enot.core.registry.EnotElementSpecification;

import java.util.Set;

public class MyCustomElementSpecification implements EnotElementSpecification {

    private static final EnotValueSpecification CONSUME_TYPE = new EnotValueSpecification(new MyCustomValueType(),
            true);
    private static final EnotValueSpecification PRODUCE_TYPE = new EnotValueSpecification(CommonEnotValueType.BINARY,
            false);

    private static final Set<EnotAttribute> REQUIRED_ATTRIBUTES = Set.of(MyCustomAttribute.TAG);
    private static final Set<EnotAttribute> ALLOWED_ATTRIBUTES = Set.of(MyCustomAttribute.TAG, MyCustomAttribute.MIN_LENGTH,
            MyCustomAttribute.MAX_LENGTH);

    @Override
    public EnotValueSpecification getConsumeType() {
        return CONSUME_TYPE;
    }

    @Override
    public EnotValueSpecification getProduceType() {
        return PRODUCE_TYPE;
    }

    @Override
    public Set<EnotAttribute> getRequiredAttributes() {
        return REQUIRED_ATTRIBUTES;
    }

    @Override
    public Set<EnotAttribute> getAllowedAttributes() {
        return ALLOWED_ATTRIBUTES;
    }

    @Override
    public EnotElementBodyResolver getBodyResolver() {
        return null;
    }
}
```

### getElementValidator

The eNot parser performs the following built-in validation automatically based on `EnotAttribute` and `EnotElementSpecification`:
- required attributes are present; no attributes outside the allowed set appear
- attribute values match their declared types
- the element body type is compatible with the declared consume type (placeholders are exempt — they can only be resolved at serialization time)
- the produced type is compatible with what the parent element expects

If these built-in checks are sufficient for your element type — for example, when all attributes are simple primitives with no cross-attribute constraints — `getElementValidator()` may return `null`. No custom validator will be invoked; only the automatic checks above will apply.

In addition to the above, element-specific attribute semantics must be validated in your own `EnotElementValidator` implementation. In this example, the element has `TAG`, `MIN_LENGTH`, and `MAX_LENGTH` attributes that require the following custom checks:
- `TAG` — must be a valid hex string
- `MIN_LENGTH` — must be a non-negative integer
- `MAX_LENGTH` — must be a non-negative integer
- `MIN_LENGTH` must be ≤ `MAX_LENGTH`

```java
package my.custom.type;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

public class MyCustomElementValidator implements EnotElementValidator {

    private static final int MAX_TAG_LENGTH = 4;

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        String tagPath = attributesPath + "/" + MyCustomAttribute.TAG.getName();

        Object tagObject = element.getAttribute(MyCustomAttribute.TAG);
        if (tagObject == null) {
            jsonErrors.add(EnotJsonError.of(tagPath, "missing " + MyCustomAttribute.TAG.getName() + " attribute for " +
                    "eNot element of type " + MyCustomEnotTypeSpecification.TYPE));
            return;
        }

        byte[] tagBinary;
        if (tagObject instanceof String tag) {
            try {
                tagBinary = HexFormat.of().parseHex(tag);
            } catch (Exception e) {
                jsonErrors.add(EnotJsonError.of(tagPath, MyCustomAttribute.TAG.getName() + " attribute for eNot element of type "
                        + MyCustomEnotTypeSpecification.TYPE + " must be hex string"));
                return;
            }
        } else {
            jsonErrors.add(EnotJsonError.of(tagPath, MyCustomAttribute.TAG.getName() + " attribute for eNot element of type "
                    + MyCustomEnotTypeSpecification.TYPE + " must be hex string"));
            return;
        }

        if (tagBinary.length == 0) {
            jsonErrors.add(EnotJsonError.of(tagPath, MyCustomAttribute.TAG.getName() + " attribute for eNot element of type "
                    + MyCustomEnotTypeSpecification.TYPE + " must not be empty"));
        } else if (tagBinary.length > MAX_TAG_LENGTH) {
            jsonErrors.add(EnotJsonError.of(tagPath, "length of " + MyCustomAttribute.TAG.getName() + " attribute for eNot element of type "
                    + MyCustomEnotTypeSpecification.TYPE + " must not exceed 4 bytes or 8 octets"));
        }

        String minLengthPath = attributesPath + "/" + MyCustomAttribute.MIN_LENGTH.getName();
        Object minLengthObject = element.getAttribute(MyCustomAttribute.MIN_LENGTH);
        Integer minLength = null;
        if (minLengthObject != null) {
            if (minLengthObject instanceof Number minLengthInt) {
                if (minLengthInt.intValue() < 0) {
                    jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + MyCustomAttribute.MIN_LENGTH.getName()
                            + " value must be non-negative integer"));
                } else {
                    minLength = minLengthInt.intValue();
                }
            } else {
                jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + MyCustomAttribute.MIN_LENGTH.getName()
                        + " value must be non-negative integer"));
            }
        }

        String maxLengthPath = attributesPath + "/" + MyCustomAttribute.MAX_LENGTH.getName();
        Object maxLengthObject = element.getAttribute(MyCustomAttribute.MAX_LENGTH);
        Integer maxLength = null;
        if (maxLengthObject != null) {
            if (maxLengthObject instanceof Number maxLengthInt) {
                if (maxLengthInt.intValue() < 0) {
                    jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + MyCustomAttribute.MAX_LENGTH.getName()
                            + " value must be non-negative integer"));
                } else {
                    maxLength = maxLengthInt.intValue();
                }
            } else {
                jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + MyCustomAttribute.MAX_LENGTH.getName()
                        + " value must be non-negative integer"));
            }
        }

        if (minLength != null && maxLength != null) {
            if (minLength > maxLength) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + MyCustomAttribute.MIN_LENGTH.getName()
                        + " must be less or equals " + MyCustomAttribute.MAX_LENGTH.getName()));
            }
        }
    }
}
```

The validator collects attribute errors during the parsing phase. In example above:
- the `tag` attribute is extracted and validated as a non-empty hex string within the allowed length
- the optional `min_length` and `max_length` attributes are extracted and validated as non-negative integers
- when both are present, `min_length` ≤ `max_length` is enforced

During the parsing phase, eNot does not stop at the first error. Instead, it collects all errors and returns them together, so that users can fix all problems at once. Errors are added to the `jsonErrors` list rather than thrown immediately:

```java
jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + MyCustomAttribute.MIN_LENGTH.getName()
                        + " must be less or equals " + MyCustomAttribute.MAX_LENGTH.getName()));
```

The first argument of `EnotJsonError.of` is a JSON pointer to the location in the template where the error occurred; the second argument is the human-readable error message.

### getSerializer

Finally, we get to the method that returns the serializer for a given eNot element. There are three options:

- **`SimpleElementSerializer`** — the most common choice. The framework serializes the body first, then passes the results to your `serialize()` method. Use this when your element just needs to wrap or transform the serialized body.
- **`BaseElementSerializer`** — use this when you need to decide whether or how to serialize the body before it happens. The system `condition` serializer is an example: it evaluates the condition expression first, and only serializes the body if the condition is true.
- **`ElementSerializer`** — use this for full control over both body serialization and the final output.

The example below extends `SimpleElementSerializer`. The element prepends the tag bytes to the serialized body, so no pre-body control or custom body serialization is needed:

```java
package my.custom.type;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import io.github.flexca.enot.core.serializer.ElementSerializationResult;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.SimpleElementSerializer;
import io.github.flexca.enot.core.util.BinaryUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

public class MyCustomElementSerializer extends SimpleElementSerializer {

    @Override
    protected List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                         String jsonPath) throws EnotSerializationException {

        // This follows common eNot logic - when element is optional and its body is empty then we do not serializing this element,
        // when element is mandatory then EnotSerializationException must be thrown 
        if (CollectionUtils.isEmpty(serializedBody)) {
            if (element.isOptional()) {
                return Collections.emptyList();
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME,
                        "missing required body for non optional element"));
            }
        }

        byte[] body = extractBody(serializedBody, jsonPath);
        int bodyLength = body.length;

        String attributesPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        byte[] tag = extractTag(element, attributesPath);
        Integer minValue = getMinLength(element, attributesPath);
        Integer maxValue = getMaxLength(element, attributesPath);
        String bodyPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;

        // When min_length attribute set we're validating of body length:
        if(minValue != null && bodyLength < minValue) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                    "element value length " + bodyLength + " is less than required "
                            + MyCustomAttribute.MIN_LENGTH.getName() + " (" + minValue + ")"));
        }
        // When max_length attribute set we're validating of body length:
        if(maxValue != null && bodyLength > maxValue) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                    "element value length " + bodyLength + " exceeds "
                            + MyCustomAttribute.MAX_LENGTH.getName() + " (" + maxValue + ")"));
        }

        byte[] result = BinaryUtils.concatenateBinary(tag, body);

        return Collections.singletonList(ElementSerializationResult.of(CommonEnotValueType.BINARY, result));
    }

    private byte[] extractTag(EnotElement element, String jsonPath) throws EnotSerializationException {

        Object tagObject = element.getAttribute(MyCustomAttribute.TAG);
        String tagPath = jsonPath + "/" + MyCustomAttribute.TAG.getName();
        if (tagObject == null) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "missing required attribute [" + MyCustomAttribute.TAG.getName() + "]"));
        }
        if (!(tagObject instanceof String)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "attribute [" + MyCustomAttribute.TAG.getName() + "] must be hex string"));
        }

        try {
            return HexFormat.of().parseHex((String) tagObject);
        } catch(Exception e) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(tagPath,
                    "attribute [" + MyCustomAttribute.TAG.getName() + "] must be hex string"));
        }
    }

    private byte[] extractBody(List<ElementSerializationResult> serializedBody, String jsonPath) throws EnotSerializationException {

        String bodyJsonPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;

        List<byte[]> binaries = new ArrayList<>();
        for (ElementSerializationResult item : serializedBody) {
            if (item.getValueType().haveSuper(CommonEnotValueType.BINARY)) {
                EnotBinaryConverter binaryConverter = item.getValueType().getBinaryConverter();
                if (binaryConverter == null) {
                    throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyJsonPath,
                            "binary convertor not found for type " + item.getValueType().getName()));
                }
                binaries.add(binaryConverter.toBinary(item.getData()));
            } else {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                        + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "unsupported body type, expecting binary input"));
            }
        }

        if (binaries.isEmpty()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath
                    + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME, "body must not be empty"));
        }

        return BinaryUtils.concatenateBinary(binaries);
    }

    private Integer getMinLength(EnotElement element, String currentJsonPath) throws EnotSerializationException {

        Object minLengthObject = element.getAttribute(MyCustomAttribute.MIN_LENGTH);
        if (minLengthObject == null) {
            return null;
        }

        if (minLengthObject instanceof Number minLengthNumber) {
            int minValue = minLengthNumber.intValue();
            if (minValue < 0) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                        + "/" + MyCustomAttribute.MIN_LENGTH.getName(), "attribute [" + MyCustomAttribute.MIN_LENGTH.getName()
                        + "] must be non-negative integer"));
            }
            return minValue;
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                    + "/" + MyCustomAttribute.MIN_LENGTH.getName(), "attribute [" + MyCustomAttribute.MIN_LENGTH.getName()
                    + "] must be non-negative integer"));
        }
    }

    private Integer getMaxLength(EnotElement element, String currentJsonPath) throws EnotSerializationException {

        Object maxLengthObject = element.getAttribute(MyCustomAttribute.MAX_LENGTH);
        if (maxLengthObject == null) {
            return null;
        }

        if (maxLengthObject instanceof Number maxLengthNumber) {
            int maxValue = maxLengthNumber.intValue();
            if (maxValue < 0) {
                throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                        + "/" + MyCustomAttribute.MAX_LENGTH.getName(), "attribute [" + MyCustomAttribute.MAX_LENGTH.getName()
                        + "] must be non-negative integer"));
            }
            return maxValue;
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(currentJsonPath
                    + "/" + MyCustomAttribute.MAX_LENGTH.getName(), "attribute [" + MyCustomAttribute.MAX_LENGTH.getName()
                    + "] must be non-negative integer"));
        }
    }
}
```

What `serialize()` does:
- If the body is empty and the element is optional, serialization is skipped and an empty list is returned. If the element is mandatory, an `EnotSerializationException` is thrown.
- The body results are concatenated into a single binary value.
- The optional `min_length` and `max_length` attributes are read and, if present, the body length is validated against them.
- The tag bytes are prepended to the body and the result is returned.

## Registration of MyCustomEnotTypeSpecification in EnotRegistry and usage in YAML or JSON

Final step is to register you custom element type in EnotRegistry:

```java
EnotRegistry enotRegistry = new EnotRegistry.Builder()
        .withTypeSpecifications(new SystemTypeSpecification())
        .withTypeSpecifications(new MyCustomEnotTypeSpecification())
        .build();
```

Now you can add it to your YAML or JSON templates:

```yaml
type: my-custom-type
attributes:
  tag: "abcd"
  max_length: 64
body: 
  type: system
  attributes: 
    kind: hex_to_bin
  body: "11223344556677889900"
```

---

## Summary: implementation checklist

To implement a working custom element type, create the following six classes in this order:

1. **`MyCustomValueType`** (`EnotValueType`) — define the value type your element body accepts, including its binary converter and supertypes.
2. **`MyCustomAttribute`** (`EnotAttribute`) — declare the attributes your element exposes and their value types. Use a `CommonEnotValueType` value (not your custom type) for attributes where `isAllowedForAttributes()` must be `true`.
3. **`MyCustomElementSpecification`** (`EnotElementSpecification`) — declare the consume type, produce type, and required/allowed attributes.
4. **`MyCustomElementValidator`** (`EnotElementValidator`) — implement custom semantic validation beyond the built-in checks. Return `null` from `getElementValidator()` if no custom validation is needed.
5. **`MyCustomElementSerializer`** (e.g. `SimpleElementSerializer`) — implement the serialization logic.
6. **`MyCustomEnotTypeSpecification`** (`EnotTypeSpecification`) — wire all of the above together and register with `EnotRegistry`.
