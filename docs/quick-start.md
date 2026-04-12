# Quick Start

## Requirements

- Java 17+
- Maven 3.8+

---

## 1. Add the dependency

> **Note:** eNot is not yet published to Maven Central. Until the first release, build locally with `mvn install` and reference the snapshot:

```xml
<dependency>
    <groupId>com.github.flexca.enot</groupId>
    <artifactId>enot-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 2. Write a template

Create a JSON template. This example encodes an X.509 Subject DN Common Name as an ASN.1 SET:

```json
{
  "type": "asn.1",
  "attributes": { "tag": "set" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "sequence" },
    "body": [
      {
        "type": "asn.1",
        "attributes": { "tag": "object_identifier" },
        "body": "2.5.4.3"
      },
      {
        "type": "asn.1",
        "attributes": { "tag": "utf8_string" },
        "body": "${common_name}"
      }
    ]
  }
}
```

`${common_name}` is a placeholder — its value is supplied at serialization time.

---

## 3. Parse and serialize

```java
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import com.github.flexca.enot.core.element.EnotElement;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

// 1. Build the registry — register the type specifications you need
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecifications(
                new SystemTypeSpecification(),   // loop, condition, bit_map, …
                new Asn1TypeSpecification()       // asn.1 tags
        )
        .build();

ObjectMapper objectMapper = new ObjectMapper();

// 2. Parse the template JSON into an element tree
EnotParser parser = new EnotParser(registry, objectMapper);
List<EnotElement> elements = parser.parse(templateJson);   // templateJson is a String

// 3. Serialize — provide placeholder values as a plain Map
EnotSerializer serializer = new EnotSerializer(registry, parser);
List<byte[]> encoded = serializer.serialize(elements, Map.of("common_name", "Alice"));

// encoded.get(0) now contains the DER-encoded SET
byte[] der = encoded.get(0);
```

---

## 4. Supplying nested parameters (LOOP)

When a template contains a `loop` element, pass the corresponding value as a `List`:

```java
List<byte[]> encoded = serializer.serialize(elements, Map.of(
        "organizational_units", List.of(
                Map.of("unit", "Engineering"),
                Map.of("unit", "Security")
        )
));
```

---

## 5. Conditional encoding

Templates can select encoding based on a condition expression:

```json
{
  "type": "system",
  "attributes": {
    "kind": "condition",
    "expression": "${expires_on} < '2050-01-01T00:00:00Z'"
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utc_time" },
    "body": "${expires_on}"
  }
}
```

The `body` is only serialized when the expression evaluates to `true`.  
See the [Format Reference](format.md#condition) for the full expression syntax.

---

## Next steps

- [Format overview](format/) — element structure, values, placeholders
- [ASN.1 elements](format/asn1.md) — all tags and body types
- [System elements](format/system.md) — loop, condition, bit_map, sha1, …
- [Expression syntax](format/expressions.md) — operators, functions, type rules
