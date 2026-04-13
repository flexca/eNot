# eNot — Encoding Notations

[![Build](https://github.com/flexca/eNot/actions/workflows/maven-test.yml/badge.svg)](https://github.com/flexca/eNot/actions/workflows/maven-test.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)

**eNot** (**Encoding Notations**) is a general purpose templating engine that serializes structured data into binary formats such as **ASN.1** and **BER-TLV**.

A template describes the binary structure declaratively in JSON. At serialization time, placeholders are resolved from a parameter map, control structures (loops, conditions) are evaluated, and the result is encoded into the target binary format.

---

## Why eNot?

Most PKI and smart-card tooling either hard-codes binary structures or requires developers to work directly with low-level ASN.1 / BER-TLV APIs. eNot sits in between: templates are human-readable JSON files that can be version-controlled, reviewed, and reused, while the engine handles all binary encoding details.

---

## Modules

| Module | Description |
|--------|-------------|
| `core` | Parser, serializer, expression engine, type registry |
| `ber-tlv` | BER-TLV extension (PoC — shows eNot is not ASN.1-only) |
| `web-tool` | Browser-based template editor and serializer (planned) |

---

## Quick start

→ **[Quick Start Guide](docs/quick-start.md)**

---

## Documentation

| Document                                        | Description |
|-------------------------------------------------|-------------|
| [Quick Start](docs/quick-start.md)              | Add the dependency, parse a template, serialize with parameters |
| [Format overview](docs/format/README.md)        | Element structure, values, placeholders |
| [ASN.1 elements](docs/format/asn1.md)           | All ASN.1 tags and accepted body types |
| [System elements](docs/format/system.md)        | loop, condition, bit_map, sha1, hex_to_bin, bin_to_hex, … |
| [Expression syntax](docs/format/expressions.md) | Operators, functions, type rules |

---

## Requirements

- Java 17+
- Maven 3.8+

---

## License

Apache License 2.0 — see [LICENSE](LICENSE).
