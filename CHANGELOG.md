# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-05-03

### Added

#### Core — ASN.1 DER encoding

- JSON and YAML template support for describing binary structures
- Parameterised placeholder syntax (`${name}`) with runtime value substitution
- `optional` element flag — silently skips elements whose value is absent from the params map
- Nested element bodies — the encoded output of one element can be the body of another
- Scope-aware placeholder resolution: loop scope, group scope, and `global.` prefix for top-level access

**Structural tags**
- `sequence` — DER SEQUENCE, ordered collection of child elements
- `set` — DER SET, unordered collection

**Primitive tags**
- `object_identifier` — dotted-decimal OID; supports `allowed_values` validation
- `boolean` — DER BOOLEAN (`true` → `0xFF`, `false` → `0x00`)
- `integer` — DER INTEGER, supports int / long / BigInteger values
- `octet_string` — DER OCTET STRING; supports `min_length` / `max_length` constraints
- `bit_string` — DER BIT STRING; supports `apply_padding` for named-bit-list types
- `null` — DER NULL (`05 00`), no body required

**String tags** — all support `min_length`, `max_length`, and `allowed_values` constraints
- `utf8_string` — full Unicode (UTF-8)
- `printable_string` — PrintableString character set
- `ia5_string` — 7-bit ASCII (IA5)
- `visible_string` — printable ASCII (VisibleString)
- `bmp_string` — UCS-2 / BMP plane (U+0000–U+FFFF)

**Time tags**
- `generalized_time` — DER GeneralizedTime (required for dates ≥ 2050 per RFC 5280)
- `utc_time` — DER UTCTime (required for dates before 2050 per RFC 5280)

**Context-tagging**
- `tagged_object` — wraps body in a context-specific `[n]` tag; supports `implicit` and `explicit` tagging

#### Core — System elements

- `group` — pushes a named sub-object scope
- `loop` — iterates over a list parameter, encoding the body element once per entry
- `condition` — conditionally includes a child element based on a boolean expression
- `reference` — resolves a named template from the registry and encodes it inline
- `hex_to_bin` — decodes a hex string into raw bytes
- `bin_to_hex` — encodes raw bytes as a hex string
- `sha1` — computes the SHA-1 digest of the body bytes
- `bit_map` — assembles a bitmask from a list of named boolean flags

#### Core — Extensibility

- `EnotRegistry` / `EnotRegistry.Builder` — registry for type specifications and named templates
- `TypeSpecification` SPI — register custom element types without modifying the library
- `Asn1TypeSpecification` — bundles all 16 ASN.1 tags; register once to enable all
- `SystemTypeSpecification` — bundles all 8 system elements; register once to enable all

#### BER-TLV module

- Pluggable BER-TLV encoding support

#### Web tool

- Browser-based serialization playground (`web-tool` module)
- Side-by-side Template and Params editors with YAML / JSON format switching
- **Example Params** button — auto-generates a params skeleton from the current template
- Base64-encoded output display
- Inline error panel for template and serialization errors
- Runnable via Docker or as a standalone JAR
