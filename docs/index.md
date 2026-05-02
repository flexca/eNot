# eNot Documentation

## Where to start

| I want to… | Go to |
|------------|-------|
| Understand how eNot templates are structured | [eNot format reference](enot.md) |
| Add eNot to my project and call the API | [Usage guide](enot-usage.md) |
| Find a specific ASN.1 tag | [ASN.1 elements](asn1/index.md) |
| Find a specific system element | [System elements](system/index.md) |
| Try eNot without writing any Java | [Web tool](../web-tool/README.md) |
| Add my own encoding type | [Adding a new element type](add-new-element-type.md) |

---

## All documents

### Learning eNot

- **[eNot format reference](enot.md)** — What an element is, how the four fields work (`type`, `attributes`, `body`, `optional`), and six progressive examples translating real ASN.1 structures into eNot templates.

- **[Usage guide](enot-usage.md)** — How to initialize `EnotRegistry` and `Enot`, supply parameters via `SerializationContext`, understand global params, and use all public `Enot` methods. Includes a Spring configuration example.

### Element reference

- **[ASN.1 elements](asn1/index.md)** — All 16 supported DER tags: structural, primitive, string, time, and context-tagging. Each tag has its own page with attributes, body rules, and examples.

- **[System elements](system/index.md)** — Control-flow and transformation elements: `loop`, `condition`, `group`, `reference`, `hex_to_bin`, `bin_to_hex`, `sha1`, `bit_map`.

### Extending eNot

- **[Adding a new element type](add-new-element-type.md)** — Step-by-step guide to implementing `EnotTypeSpecification` and registering a custom encoding type alongside the built-in ones.

### Modules

- **[BER-TLV module](../ber-tlv/README.md)** — BER-TLV encoding support. Plug-in module demonstrating the extensible type system.

- **[Web tool](../web-tool/README.md)** — Browser-based interactive playground. Run with Docker or as a standalone JAR to evaluate eNot templates without writing any Java.
