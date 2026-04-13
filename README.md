# eNot — Encoding Notations

[![Build](https://github.com/flexca/eNot/actions/workflows/maven-test.yml/badge.svg)](https://github.com/flexca/eNot/actions/workflows/maven-test.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)

**eNot** (**Encoding Notations**) is a JSON-driven templating engine that serializes structured data into binary formats — primarily **ASN.1 DER**, with **BER-TLV** support showing the design is not limited to a single format.

---

## The problem it solves

Whenever an application needs to produce structured binary data — network protocols, smart-card commands, certificate fields, device provisioning payloads — the choices are usually the same: reach for a heavy framework that makes all the structural decisions for you, or write low-level encoding code that is brittle, hard to review, and even harder to reuse across projects.

eNot takes a different approach. The binary structure is described as a plain JSON template:

```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": "${common_name}"
}
```

At serialization time you supply the values:

```java
Enot enot = new Enot(registry, objectMapper);
List<byte[]> der = enot.serialize(templateJson,
        new SerializationContext.Builder(objectMapper)
                .withParam("common_name", "Alice")
                .build());
```

That's it. The engine resolves `${common_name}`, encodes the UTF-8 string as DER, and returns the bytes. No hard-coded structures, no framework lock-in.

---

## Going further — loops, conditions, and composition

Binary structures are rarely flat. eNot handles this with built-in control-flow elements.

**Loops** iterate over an array of parameters and produce one encoded element per entry — any repeated structure maps directly to a loop:

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

**Conditions** encode a body only when an expression evaluates to `true`. The body is simply skipped when the expression is false, so a single template can cover multiple encoding variants depending on the input. One example is encoding a date in a different format depending on its value (as RFC 5280 requires for X.509 validity dates):

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

**References** let one template include another by identifier at parse time, so large or complex structures are assembled from smaller, independently testable pieces rather than one monolithic file — the same composability principle that makes software maintainable applies directly to binary templates.

---

## Why templates in JSON?

The format was a deliberate choice:
- Templates are **plain text** — version-controlled, diff-able, reviewable in a pull request
- The structure mirrors the binary output — a `sequence` wrapping a `set` wrapping a `utf8_string` is exactly how it looks in the JSON tree
- The engine is **format-agnostic** — the same parser and serializer infrastructure drives both the `asn.1` and `ber-tlv` type systems; new formats plug in via `EnotRegistry`
- Placeholder resolution, condition evaluation, and loop iteration are all **handled by the engine**, not scattered across application code

---

## Status

> **eNot is not yet published to Maven Central.** Build locally first:
> ```
> mvn install
> ```
> Then reference the snapshot version (see [Quick Start](docs/quick-start.md)).

---

## Modules

| Module | Description |
|--------|-------------|
| `core` | Parser, serializer, expression engine, type registry |
| `ber-tlv` | BER-TLV type extension — proof that the engine is not tied to ASN.1 |

---

## Building

Requirements: **Java 17+**, **Maven 3.8+**

```
mvn install          # build and run all tests
mvn -pl core test    # run only core module tests
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Quick Start](docs/quick-start.md) | Dependency, first template, serialization |
| [Format overview](docs/format/README.md) | Element structure, values, placeholders, scoping |
| [ASN.1 elements](docs/format/asn1.md) | All tags and accepted body types |
| [System elements](docs/format/system.md) | loop, condition, group, reference, bit_map, sha1, … |
| [Expression syntax](docs/format/expressions.md) | Operators, functions, type rules |

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).
