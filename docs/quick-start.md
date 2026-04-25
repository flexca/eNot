# Quick Start

## Requirements

- Java 17+
- Maven 3.8+

---

## 1. Add the dependency

> **Note:** eNot is not yet published to Maven Central. Until the first release, clone the repository and install locally:
> ```
> git clone https://github.com/flexca/eNot.git
> cd eNot
> mvn install
> ```

Then reference the snapshot in your project:

```xml
<dependency>
    <groupId>io.github.flexca.enot</groupId>
    <artifactId>enot-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 2. Write a template

A template is a JSON file that describes the binary structure. Elements have a `type` (the encoding engine), `attributes` (configuration), and a `body` (the payload). Placeholders like `${common_name}` are resolved from parameters at serialization time.

This example encodes a named field — an OID paired with a UTF-8 string value — wrapped in a SEQUENCE inside a SET (a common pattern in ASN.1 attribute structures):

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

---

## 3. Parse and serialize

```java
import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

// 1. Build the registry — register the type specifications you need
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecifications(
                new SystemTypeSpecification(),   // loop, condition, group, …
                new Asn1TypeSpecification()       // asn.1 tags
        )
        .build();

ObjectMapper objectMapper = new ObjectMapper();

// 2. Create the Enot facade — wires parser, serializer and expression engine
Enot enot = new Enot.Builder()
        .withRegistry(registry)
        .withJsonObjectMapper(objectMapper)
        .build();

// 3. Build a serialization context with placeholder values
//    Params can be supplied as individual entries, a Map, or a JSON string —
//    all three forms can be mixed on the same builder.
SerializationContext context = new SerializationContext.Builder(objectMapper)
        .withParam("common_name", "Alice")           // single entry
        .withParams(Map.of("country", "US"))          // from a Map
        .withParams("{\"org\": \"Example Inc\"}")     // from a JSON string
        .build();

// 4. Serialize — returns one byte[] per top-level element produced by the template.
//    A flat template produces one entry; a LOOP at the root produces one per iteration.
List<byte[]> encoded = enot.serialize(templateJson, context);

byte[] der = encoded.get(0);
```

---

## 4. Repeating structures (LOOP)

When a structure repeats for each item in a list, use a `loop` element. Mark it `optional: true` so it produces an empty result — rather than an error — when the array parameter is absent.

Template:

```json
{
  "type": "system",
  "optional": true,
  "attributes": { "kind": "loop", "items_name": "entries" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${value}"
  }
}
```

Inside the loop body, `${value}` (and any other placeholder) is resolved from the **current iteration's map entry**. Pass the array under the `items_name` key:

```java
SerializationContext context = new SerializationContext.Builder(objectMapper)
        .withParam("entries", List.of(
                Map.of("value", "first"),
                Map.of("value", "second")
        ))
        .build();

List<byte[]> encoded = enot.serialize(templateJson, context);
// encoded.size() == 2
```

---

## 5. Conditional encoding

A `condition` element serializes its body only when an expression evaluates to `true`; otherwise it produces no output. This lets a single template cover multiple encoding variants depending on the input.

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

Here the body is encoded as `UTCTime` only when the date is before 2050 — the encoding rule required by RFC 5280 for X.509 validity dates, and a good illustration of how conditions work for any date-driven encoding choice.

See [Expression syntax](format/expressions.md) for all available operators and functions.

---

## Next steps

- [Format overview](format/) — element structure, values, placeholders, scoping rules
- [ASN.1 elements](format/asn1.md) — all tags and accepted body types
- [System elements](format/system.md) — loop, condition, group, reference, bit_map, sha1, …
- [Expression syntax](format/expressions.md) — operators, functions, type rules
