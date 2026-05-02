# eNot Usage Guide

← [Back to Documentation](index.md)

---

## Table of contents

- [1. Initialization](#1-initialization)
  - [1.1 Add the dependency](#11-add-the-dependency)
  - [1.2 Build the EnotRegistry](#12-build-the-enotregistry)
  - [1.3 Build the Enot facade](#13-build-the-enot-facade)
  - [1.4 Full initialization listing](#14-full-initialization-listing)
  - [1.5 Spring setup](#15-spring-setup)
- [2. Serialization](#2-serialization)
  - [2.1 SerializationContext — supplying params](#21-serializationcontext--supplying-params)
  - [2.2 Global params](#22-global-params)
  - [2.3 Full serialization example](#23-full-serialization-example)
- [3. Additional Enot methods](#3-additional-enot-methods)
  - [3.1 parse](#31-parse)
  - [3.2 serialize from pre-parsed elements](#32-serialize-from-pre-parsed-elements)
  - [3.3 getParamsExample](#33-getparamsexample)
  - [3.4 Thread safety](#34-thread-safety)

---

## 1. Initialization

### 1.1 Add the dependency

Add `enot-core` to your build. If you also need BER-TLV encoding, add `enot-ber-tlv` too.

**Maven (`pom.xml`):**

```xml
<dependency>
    <groupId>io.github.flexca</groupId>
    <artifactId>enot-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle (`build.gradle`):**

```groovy
implementation 'io.github.flexca:enot-core:1.0.0'
```

---

### 1.2 Build the EnotRegistry

`EnotRegistry` is the central registry for element types. You build it once at application startup and share it for the lifetime of the application.

```java
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;

EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
        .build();
```

**Builder methods:**

| Method | Description |
|--------|-------------|
| `withTypeSpecification(EnotTypeSpecification)` | Registers a single type specification. |
| `withTypeSpecifications(EnotTypeSpecification...)` | Registers multiple type specifications in one call (varargs). |
| `withTypeSpecifications(Collection<EnotTypeSpecification>)` | Registers a collection of type specifications. |
| `withElementReferenceResolver(EnotElementReferenceResolver)` | Registers a resolver for the `reference` system element. Needed if your templates use named template references. |
| `build()` | Constructs and returns the registry. |

**Built-in type specifications:**

| Specification | What it registers |
|--------------|-------------------|
| `SystemTypeSpecification` | All system elements: `loop`, `condition`, `group`, `reference`, `hex_to_bin`, `bin_to_hex`, `sha1`, `bit_map`. |
| `Asn1TypeSpecification` | All 16 ASN.1 DER tags. |
| `BerTlvEnotTypeSpecification` | BER-TLV encoding (from the `enot-ber-tlv` module). |

---

### 1.3 Build the Enot facade

`Enot` is the main entry point. It wraps the registry, parser, serializer, and example-params extractor into a single, reusable object. You configure it with the `ObjectMapper` instances that match the template format(s) you intend to use.

```java
import io.github.flexca.enot.core.Enot;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

ObjectMapper jsonMapper = new ObjectMapper();
ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

Enot enot = new Enot.Builder()
        .withRegistry(registry)
        .withJsonObjectMapper(jsonMapper)    // required if you use JSON templates
        .withYamlObjectMapper(yamlMapper)    // required if you use YAML templates
        .build();
```

**Builder methods:**

| Method | Description |
|--------|-------------|
| `withRegistry(EnotRegistry)` | **Required.** Sets the type registry. |
| `withJsonObjectMapper(ObjectMapper)` | Sets the mapper used to parse JSON templates and params strings. Required if you use JSON-formatted input. |
| `withYamlObjectMapper(ObjectMapper)` | Sets the mapper used to parse YAML templates and params strings. Required if you use YAML-formatted input. |
| `build()` | Builds the `Enot` instance. Throws `EnotInvalidConfigurationException` if the registry is not set or neither mapper is provided. |

> **Note:** At least one of `withJsonObjectMapper` or `withYamlObjectMapper` must be called. You can provide both, which lets you mix JSON and YAML templates freely — the format is detected automatically at runtime.

---

### 1.4 Full initialization listing

```java
import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

// 1. Build the registry
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecifications(
                new SystemTypeSpecification(),   // loops, conditions, group, reference, …
                new Asn1TypeSpecification()       // all ASN.1 DER tags
        )
        .build();

// 2. Build the Enot facade
Enot enot = new Enot.Builder()
        .withRegistry(registry)
        .withJsonObjectMapper(new ObjectMapper())
        .withYamlObjectMapper(new ObjectMapper(new YAMLFactory()))
        .build();
```

---

### 1.5 Spring setup

In a Spring application, declare `EnotRegistry` and `Enot` as beans. The following is the configuration used by the eNot web tool:

```java
import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

@Configuration
public class EnotConfig {

    @Bean
    public EnotRegistry enotRegistry() {
        return new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification())
                .withTypeSpecifications(new Asn1TypeSpecification())
                .build();
    }

    @Bean
    public Enot enot(EnotRegistry enotRegistry,
                     @Qualifier("jsonObjectMapper") ObjectMapper jsonObjectMapper,
                     @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        return new Enot.Builder()
                .withRegistry(enotRegistry)
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .build();
    }

    @Bean
    @Qualifier("jsonObjectMapper")
    public ObjectMapper jsonObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Qualifier("yamlObjectMapper")
    public ObjectMapper yamlObjectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
```

Inject `Enot` wherever you need serialization:

```java
@Service
@RequiredArgsConstructor
public class MyService {

    private final Enot enot;

    public byte[] encode(String template, String subjectCn) throws EnotException {
        SerializationContext context = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("subject_cn", subjectCn)
                .build();
        List<byte[]> result = enot.serialize(template, context);
        return result.get(0);
    }
}
```

---

## 2. Serialization

`SerializationContext` carries the parameter values used to resolve placeholders at serialization time. A new context must be created for each serialization call — instances are **not** thread-safe.

### 2.1 SerializationContext — supplying params

Parameters can be supplied in three forms, and all three can be mixed on the same builder:

**Single key-value pair:**

```java
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParam("subject_cn", "Alice")
        .withParam("country", "US")
        .build();
```

**From a `Map`:**

```java
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParams(Map.of(
                "subject_cn", "Alice",
                "serial_number", 42,
                "dns_names", List.of(
                        Map.of("value", "alice.example.com"),
                        Map.of("value", "www.example.com")
                )
        ))
        .build();
```

**From a JSON string:**

```java
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParams("{\"subject_cn\": \"Alice\", \"country\": \"US\"}")
        .build();
```

The format is detected automatically — passing a YAML string works too, as long as the matching `ObjectMapper` was set on the builder:

```java
SerializationContext context = new SerializationContext.Builder()
        .withYamlObjectMapper(yamlMapper)
        .withParams("subject_cn: Alice\ncountry: US")
        .build();
```

All three forms can be combined in one builder. Multiple `withParams` calls are **merged** — later calls add to the params rather than replacing them.

**Builder methods:**

| Method | Description |
|--------|-------------|
| `withJsonObjectMapper(ObjectMapper)` | Required when supplying params as a JSON string. |
| `withYamlObjectMapper(ObjectMapper)` | Required when supplying params as a YAML string. |
| `withParam(String key, Object value)` | Adds a single param. Keys with the `global.` prefix are automatically stored as global params. |
| `withParams(Map<String, Object>)` | Merges a map into params. Keys with the `global.` prefix are automatically stored as global params. |
| `withParams(String jsonOrYaml)` | Parses a JSON or YAML object string and merges the result into params. |
| `build()` | Returns the `SerializationContext`. |

> **Note:** Param key names must contain only letters, digits, and underscores, and must not be blank. The reserved prefix `system.` is not allowed.

---

### 2.2 Global params

A **global param** is a parameter that is visible at any scope depth regardless of `loop` or `group` nesting. It is useful for values that are constant across all iterations — such as an issuer name or environment identifier.

To set a global param, prefix the key with `global.`:

```java
// Via withParam:
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParam("global.issuer_cn", "My CA")
        .withParam("subject_cn", "Alice")
        .build();
```

```java
// Via withParams map:
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParams(Map.of(
                "subject_cn", "Alice",
                "global.issuer_cn", "My CA"
        ))
        .build();
```

```java
// Via JSON string — the global. key is auto-detected:
SerializationContext context = new SerializationContext.Builder()
        .withJsonObjectMapper(jsonMapper)
        .withParams("{\"subject_cn\": \"Alice\", \"global.issuer_cn\": \"My CA\"}")
        .build();
```

In the template, reference global params using the `global.` prefix:

```yaml
type: asn.1
attributes:
  tag: utf8_string
body: "${global.issuer_cn}"
```

Inside a `loop`, `${subject_cn}` resolves from the current iteration's map while `${global.issuer_cn}` always resolves from the global map:

```yaml
type: system
attributes:
  kind: loop
  items_name: subjects
body:
  type: asn.1
  attributes: { tag: sequence }
  body:
    - type: asn.1
      attributes: { tag: utf8_string }
      body: "${subject_cn}"        # resolved from each item in "subjects"
    - type: asn.1
      attributes: { tag: utf8_string }
      body: "${global.issuer_cn}"  # always resolved from global params
```

---

### 2.3 Full serialization example

**Template (YAML):**

```yaml
type: asn.1
attributes: { tag: sequence }
body:
  - type: asn.1
    attributes: { tag: object_identifier }
    body: "2.5.4.3"
  - type: asn.1
    attributes: { tag: utf8_string }
    body: "${subject_cn}"
  - type: asn.1
    optional: true
    attributes: { tag: utf8_string }
    body: "${subject_email}"
```

**Template (JSON equivalent):**

```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    { "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.4.3" },
    { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${subject_cn}" },
    { "type": "asn.1", "optional": true, "attributes": { "tag": "utf8_string" }, "body": "${subject_email}" }
  ]
}
```

**Serialization:**

```java
import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.exception.EnotException;
import io.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.Base64;
import java.util.List;

SerializationContext context = new SerializationContext.Builder()
        .withYamlObjectMapper(yamlMapper)
        .withParam("subject_cn", "Alice")
        // subject_email not provided — element is optional, so it is silently skipped
        .build();

try {
    List<byte[]> result = enot.serialize(templateYaml, context);
    String base64 = Base64.getEncoder().encodeToString(result.get(0));
    System.out.println(base64);
} catch (EnotException e) {
    // EnotParsingException  — template has structural errors
    // EnotSerializationException — a required placeholder value was missing
    e.printStackTrace();
}
```

`enot.serialize(...)` returns `List<byte[]>` — one entry per top-level element in the template. A single-root template always returns a list of one. A `loop` at the root returns one entry per iteration.

---

## 3. Additional Enot methods

### 3.1 parse

`parse` converts a template string into a `List<EnotElement>` without serializing. This is useful when you want to validate or inspect a template, or when you intend to serialize the same parsed structure multiple times with different parameter sets.

```java
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotParsingException;

try {
    List<EnotElement> elements = enot.parse(templateJson);
} catch (EnotParsingException e) {
    // Template has structural or type errors — e.getMessage() describes the first error
    e.printStackTrace();
}
```

Format (JSON or YAML) is detected automatically.

---

### 3.2 serialize from pre-parsed elements

If you have already called `parse`, you can serialize the `List<EnotElement>` directly:

```java
List<EnotElement> elements = enot.parse(templateJson);

// Serialize once per context — reuse the same parsed elements
List<byte[]> result1 = enot.serialize(elements, context1);
List<byte[]> result2 = enot.serialize(elements, context2);
```

This avoids re-parsing the template on every call — useful in high-throughput code paths.

---

### 3.3 getParamsExample

`getParamsExample` inspects a template and returns a skeleton map populated with placeholder names and a `"replace with your value"` sentinel for each one. This is the same feature the web tool's **Example Params** button uses.

Three overloads are available:

```java
// From a template string
Map<String, Object> example = enot.getParamsExample(templateJson);

// From a pre-parsed element
List<EnotElement> elements = enot.parse(templateJson);
Map<String, Object> example = enot.getParamsExample(elements.get(0));

// From a list of pre-parsed elements
Map<String, Object> example = enot.getParamsExample(elements);
```

To get the result as a JSON string instead of a `Map`:

```java
String exampleJson = enot.getParamsExampleJson(templateJson);
// e.g. {"subject_cn":"replace with your value","country":"replace with your value"}
```

This is handy for building tooling, debugging, or generating test fixtures from real templates.

---

### 3.4 Thread safety

| Object | Thread safety |
|--------|--------------|
| `EnotRegistry` | Thread-safe. Immutable after construction — safe to share. |
| `Enot` | Thread-safe after construction. All mutable state lives in `SerializationContext`. |
| `SerializationContext` | **Not thread-safe.** Create a new instance for every serialization call. |

