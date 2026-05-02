# eNot Format Reference

← [Back to Documentation](index.md)

---

An eNot template is a JSON or YAML document that describes a binary structure declaratively. The engine walks the element tree, resolves placeholder values from a parameter map, evaluates conditions and loops, and produces encoded binary output.

---

## Table of contents

- [Element structure](#element-structure)
  - [type](#type)
  - [attributes](#attributes)
  - [body](#body)
  - [optional](#optional)
- [Example 1 — Single primitive value](#example-1--single-primitive-value)
- [Example 2 — Named field (OID + value)](#example-2--named-field-oid--value)
- [Example 3 — Optional field](#example-3--optional-field)
- [Example 4 — Repeating structure](#example-4--repeating-structure)
- [Example 5 — Conditional encoding](#example-5--conditional-encoding)
- [Example 6 — Processing pipeline](#example-6--processing-pipeline)

---

## Element structure

Every building block in an eNot template is an **element** — a JSON object or YAML mapping with up to four fields:

```yaml
type: asn.1           # required — selects the encoding engine
attributes:           # required — engine-specific configuration
  tag: utf8_string
body: "${common_name}" # required — the payload to encode
optional: true        # optional — skip silently when body value is absent
```

```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": "${common_name}",
  "optional": true
}
```

---

### type

`type` selects which encoding engine processes this element.

| Value | Description |
|-------|-------------|
| `"asn.1"` | ASN.1 DER encoding via BouncyCastle. The `tag` attribute selects the specific DER type. See [ASN.1 elements](asn1/index.md). |
| `"system"` | Control flow and data transformation. The `kind` attribute selects the behaviour. See [System elements](system/index.md). |
| `"ber-tlv"` | BER-TLV encoding. See the `ber-tlv` module. |

The type system is open — custom types can be registered via `EnotRegistry`.

---

### attributes

`attributes` is a key-value object that configures the chosen `type`. The available keys depend entirely on the type:

- For `"asn.1"`, the required attribute is `tag`:
  ```yaml
  attributes:
    tag: sequence
  ```
- For `"system"`, the required attribute is `kind`:
  ```yaml
  attributes:
    kind: loop
    items_name: entries
  ```

Each type documents its own required and optional attributes. See [ASN.1 elements](asn1/index.md) and [System elements](system/index.md).

---

### body

`body` is the payload the engine will encode. It accepts four forms:

| Form | When to use |
|------|-------------|
| String literal — `"2.5.4.3"` | Value is known at template-write time |
| Placeholder — `"${common_name}"` | Value is supplied at serialization time |
| Nested element — `{ "type": "asn.1", ... }` | The encoded output of a child element is the payload |
| Array of elements — `[ { "type": ... }, ... ]` | Structural types that contain multiple children (SEQUENCE, SET, loop) |

---

### optional

When `optional: true`, the element is **silently skipped** if the placeholder it references is absent from the params map. No bytes are written and no error is raised.

```yaml
type: asn.1
optional: true
attributes:
  tag: utf8_string
body: "${middle_name}"   # omitted entirely when middle_name is not in params
```

Without `optional: true`, a missing placeholder value causes a serialization error.

Default: `false`.

---

## Example 1 — Single primitive value

**ASN.1 definition:**
```asn1
commonName UTF8String
```

The simplest possible template: one element, one placeholder.

**YAML:**
```yaml
type: asn.1
attributes:
  tag: utf8_string
body: "${common_name}"
```

**JSON:**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": "${common_name}"
}
```

**Params:**
```json
{ "common_name": "Alice" }
```

The engine encodes the string `"Alice"` as a DER UTF8String (`0C 05 41 6C 69 63 65`).

---

## Example 2 — Named field (OID + value)

**ASN.1 definition:**
```asn1
AttributeTypeAndValue ::= SEQUENCE {
    type    OBJECT IDENTIFIER,
    value   UTF8String
}
```

A named field pairs an OID (hardcoded in the template) with a runtime string value, wrapped in a SEQUENCE inside a SET — the standard pattern for X.509 Subject DN attributes.

**YAML:**
```yaml
type: asn.1
attributes:
  tag: set
body:
  type: asn.1
  attributes:
    tag: sequence
  body:
    - type: asn.1
      attributes:
        tag: object_identifier
      body: "2.5.4.3"
    - type: asn.1
      attributes:
        tag: utf8_string
      body: "${common_name}"
```

**JSON:**
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

Key points:
- The OID `2.5.4.3` (commonName) is a literal — it never changes.
- The string value is a placeholder resolved at serialization time.
- The outer `set` wraps a single `sequence` (one child → nested element body, not an array).
- The `sequence` has two children → array body.

---

## Example 3 — Optional field

**ASN.1 definition:**
```asn1
Extension ::= SEQUENCE {
    extnID      OBJECT IDENTIFIER,
    critical    BOOLEAN OPTIONAL,
    extnValue   OCTET STRING
}
```

The `critical` field is marked `OPTIONAL` in the ASN.1 definition. In eNot, set `optional: true` on the corresponding element so it is silently omitted when not provided.

**YAML:**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.15"
  - type: asn.1
    optional: true
    attributes:
      tag: boolean
    body: "${key_usage_critical}"
  - type: asn.1
    attributes:
      tag: octet_string
    body: "${key_usage_value}"
```

**JSON:**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.15"
    },
    {
      "type": "asn.1",
      "optional": true,
      "attributes": { "tag": "boolean" },
      "body": "${key_usage_critical}"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": "${key_usage_value}"
    }
  ]
}
```

When `key_usage_critical` is absent from params, the BOOLEAN element is skipped and the SEQUENCE contains only the OID and the OCTET STRING. When it is present, the BOOLEAN is included between them.

---

## Example 4 — Repeating structure

**ASN.1 definition:**
```asn1
-- Subject DN may contain multiple Organizational Unit entries
organizationalUnit AttributeTypeAndValue ::= SEQUENCE {
    type  OBJECT IDENTIFIER,   -- 2.5.4.11
    value UTF8String
}
```

When the same structure repeats for each item in a list, use a `loop` system element. The `items_name` attribute names the parameter key holding the array. Inside the loop body, placeholders are resolved from **the current iteration's map entry**.

**YAML:**
```yaml
type: system
attributes:
  kind: loop
  items_name: organizational_units
body:
  type: asn.1
  attributes:
    tag: set
  body:
    type: asn.1
    attributes:
      tag: sequence
    body:
      - type: asn.1
        attributes:
          tag: object_identifier
        body: "2.5.4.11"
      - type: asn.1
        attributes:
          tag: utf8_string
        body: "${unit}"
```

**JSON:**
```json
{
  "type": "system",
  "attributes": {
    "kind": "loop",
    "items_name": "organizational_units"
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "set" },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "sequence" },
      "body": [
        {
          "type": "asn.1",
          "attributes": { "tag": "object_identifier" },
          "body": "2.5.4.11"
        },
        {
          "type": "asn.1",
          "attributes": { "tag": "utf8_string" },
          "body": "${unit}"
        }
      ]
    }
  }
}
```

**Params:**
```json
{
  "organizational_units": [
    { "unit": "Engineering" },
    { "unit": "Security" }
  ]
}
```

The engine iterates over `organizational_units` and produces one SET per entry. `${unit}` resolves from the current item — `"Engineering"` on the first pass, `"Security"` on the second. The result is two DER-encoded SET structures in the output list.

---

## Example 5 — Conditional encoding

**ASN.1 definition (RFC 5280):**
```asn1
Validity ::= SEQUENCE {
    notBefore  Time,
    notAfter   Time
}
Time ::= CHOICE {
    utcTime        UTCTime,        -- for dates before 2050
    generalizedTime GeneralizedTime -- for dates >= 2050
}
```

RFC 5280 requires `UTCTime` for dates before 2050 and `GeneralizedTime` for dates from 2050 onwards. A `condition` element includes its body only when its expression evaluates to `true`.

**YAML:**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: utc_time
      body: "${valid_from}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: generalized_time
      body: "${valid_from}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: utc_time
      body: "${expires_on}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: generalized_time
      body: "${expires_on}"
```

**JSON:**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"
      },
      "body": { "type": "asn.1", "attributes": { "tag": "utc_time" }, "body": "${valid_from}" }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
      },
      "body": { "type": "asn.1", "attributes": { "tag": "generalized_time" }, "body": "${valid_from}" }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"
      },
      "body": { "type": "asn.1", "attributes": { "tag": "utc_time" }, "body": "${expires_on}" }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"
      },
      "body": { "type": "asn.1", "attributes": { "tag": "generalized_time" }, "body": "${expires_on}" }
    }
  ]
}
```

For each date the engine evaluates both conditions and includes exactly one — the one whose expression is `true`. A single template handles all date combinations without any application code changes.

---

## Example 6 — Processing pipeline

**ASN.1 definition (RFC 5280 Key Usage extension):**
```asn1
KeyUsage ::= BIT STRING {
    digitalSignature  (0),
    nonRepudiation    (1),
    keyEncipherment   (2),
    dataEncipherment  (3),
    keyAgreement      (4),
    keyCertSign       (5),
    cRLSign           (6),
    encipherOnly      (7),
    decipherOnly      (8)
}

Extension ::= SEQUENCE {
    extnID    OBJECT IDENTIFIER,   -- 2.5.29.15
    critical  BOOLEAN OPTIONAL,
    extnValue OCTET STRING         -- DER encoding of KeyUsage
}
```

This is the most complex pattern: a bitmask assembled from boolean flags, wrapped in a BIT STRING, wrapped in an OCTET STRING, inside a SEQUENCE. It demonstrates how system elements and ASN.1 elements chain together into a processing pipeline.

**YAML:**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.15"
  - type: asn.1
    optional: true
    attributes:
      tag: boolean
    body: "${key_usage_critical}"
  - type: asn.1
    attributes:
      tag: octet_string
    body:
      type: asn.1
      attributes:
        tag: bit_string
        apply_padding: true
      body:
        type: system
        attributes:
          kind: bit_map
          byte_order: big_endian
          bit_order: msb_first
        body:
          - "${digital_signature}"
          - "${non_repudiation}"
          - "${key_encipherment}"
          - "${data_encipherment}"
          - "${key_agreement}"
          - "${key_cert_sign}"
          - "${crl_sign}"
          - "${encipher_only}"
          - "${decipher_only}"
```

**JSON:**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.15"
    },
    {
      "type": "asn.1",
      "optional": true,
      "attributes": { "tag": "boolean" },
      "body": "${key_usage_critical}"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "bit_string", "apply_padding": true },
        "body": {
          "type": "system",
          "attributes": {
            "kind": "bit_map",
            "byte_order": "big_endian",
            "bit_order": "msb_first"
          },
          "body": [
            "${digital_signature}",
            "${non_repudiation}",
            "${key_encipherment}",
            "${data_encipherment}",
            "${key_agreement}",
            "${key_cert_sign}",
            "${crl_sign}",
            "${encipher_only}",
            "${decipher_only}"
          ]
        }
      }
    }
  ]
}
```

**How the pipeline executes (inside out):**

1. `bit_map` assembles the nine boolean flags into a raw bitmask byte sequence. Each flag placeholder resolves to `true` or `false`; set bits correspond to enabled key usages.
2. `bit_string` wraps the bitmask in a DER BIT STRING envelope. `apply_padding: true` adds the unused-bits byte required by the named-bit-list encoding rule.
3. `octet_string` wraps the encoded BIT STRING in a DER OCTET STRING — the `extnValue` field of the extension.
4. The surrounding SEQUENCE adds the OID and the optional `critical` flag.

**Params example:**
```json
{
  "key_usage_critical": true,
  "digital_signature": true,
  "non_repudiation": false,
  "key_encipherment": true,
  "data_encipherment": false,
  "key_agreement": false,
  "key_cert_sign": false,
  "crl_sign": false,
  "encipher_only": false,
  "decipher_only": false
}
```

---

## Further reading

- [ASN.1 elements](asn1/index.md) — all 16 supported DER tags with attributes and examples
- [System elements](system/index.md) — loop, condition, group, reference, bit_map, sha1, and more
- [eNot usage guide](enot-usage.md) — how to initialize the library and call the API
