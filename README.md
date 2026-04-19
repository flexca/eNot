# eNot — Encoding Notations

[![Build](https://github.com/flexca/eNot/actions/workflows/maven-test.yml/badge.svg)](https://github.com/flexca/eNot/actions/workflows/maven-test.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/projects/jdk/17/)

**eNot** (**Encoding Notations**) is a Java templating engine that turns plain **JSON or YAML** templates into binary-encoded data — primarily **ASN.1 DER**, with **BER-TLV** support demonstrating the design is not tied to a single format.

---

## The problem it solves

Applications that produce structured binary data — PKI certificates, smart-card commands, device provisioning payloads, network protocol messages — typically choose between two bad options:

- A **heavy framework** that owns the structure and leaves no room to deviate
- **Hand-written encoding code** that is fragile, hard to review, and impossible to reuse

eNot takes a different approach: describe the binary structure as a plain text template, supply values at runtime, and let the engine handle the encoding.

**JSON template:**

```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": "${common_name}"
}
```

**Equivalent YAML template:**

```yaml
type: asn.1
attributes:
  tag: utf8_string
body: "${common_name}"
```

**Serialize it:**

```java
Enot enot = new Enot.Builder()
        .withRegistry(registry)
        .withJsonObjectMapper(new ObjectMapper())
        .build();

List<byte[]> der = enot.serialize(templateJson,
        new SerializationContext.Builder(objectMapper)
                .withParam("common_name", "Alice")
                .build());
```

The engine resolves `${common_name}`, encodes the UTF-8 string as DER, and returns the bytes. No hard-coded structures. No framework lock-in.

---

## Key features

| Feature | Description |
|---------|-------------|
| **JSON & YAML** | Both formats are supported out of the box — the engine detects which one you pass |
| **Loops** | Iterate a template body over a list of parameters — one encoded output per entry |
| **Conditions** | Encode a body only when an expression is true — cover multiple encoding variants in one template |
| **References** | Include one template inside another at parse time — compose large structures from small, testable pieces |
| **Expression engine** | Arithmetic, comparison, logical operators, and built-in functions for date and binary operations |
| **Extensible** | Any binary format plugs in via `EnotRegistry` — `asn.1` and `ber-tlv` are two separate examples |
| **Error reporting** | All parse errors are collected and reported together, not one at a time |

---

## Loops, conditions, and composition

Binary structures are rarely flat. eNot handles this with built-in control-flow elements.

**Loop** — any repeated structure maps directly to a loop:

```json
{
  "type": "system",
  "attributes": { "kind": "loop", "items_name": "dns_name" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "tagged_object", "implicit": 2 },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "ia5_string" },
      "body": "${value}"
    }
  }
}
```

**Condition** — a single template covering multiple encoding variants. This example follows RFC 5280, which requires dates before 2050 to use `UTCTime` and dates from 2050 onward to use `GeneralizedTime`:

```json
[
  {
    "type": "system",
    "attributes": { "kind": "condition", "expression": "${expires_on} < '2050-01-01T00:00:00Z'" },
    "body": { "type": "asn.1", "attributes": { "tag": "utc_time" }, "body": "${expires_on}" }
  },
  {
    "type": "system",
    "attributes": { "kind": "condition", "expression": "${expires_on} >= '2050-01-01T00:00:00Z'" },
    "body": { "type": "asn.1", "attributes": { "tag": "generalized_time" }, "body": "${expires_on}" }
  }
]
```

**References** let one template include another by identifier at parse time, so large or complex structures can be assembled from smaller, independently testable pieces.

---

## Why plain text templates?

- **Version-controlled** — templates are diff-able and reviewable in a pull request like any other source file
- **Structure mirrors output** — a `sequence` wrapping a `set` wrapping a `utf8_string` looks exactly like that in the template tree
- **Runtime values stay separate** — the template describes *shape*; the `SerializationContext` provides *values*; neither knows about the other
- **Format-agnostic core** — the same parser and serializer infrastructure drives both `asn.1` and `ber-tlv`; new formats plug in through `EnotRegistry` without touching the engine

---

## Status

> **eNot is not yet published to Maven Central.** Build locally first:
> ```
> git clone https://github.com/flexca/eNot.git
> cd eNot
> mvn install
> ```
> Then reference the snapshot version — see [Quick Start](docs/quick-start.md) for the full setup.

---

## Modules

| Module | Description |
|--------|-------------|
| `core` | Parser, serializer, expression engine, type registry |
| `ber-tlv` | BER-TLV type extension — plug-in format example |

---

## Building

Requirements: **Java 17+**, **Maven 3.8+**

```
mvn install          # build everything and run all tests
mvn -pl core test    # run only core module tests
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Quick Start](docs/quick-start.md) | Build the registry, write a template, serialize |
| [Format overview](docs/format/README.md) | Element structure, values, placeholders, scoping |
| [ASN.1 elements](docs/format/asn1.md) | All supported tags and accepted body types |
| [System elements](docs/format/system.md) | loop, condition, group, reference, bit_map, sha1, … |
| [Expression syntax](docs/format/expressions.md) | Operators, functions, type rules |

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).
