# ASN.1 elements

Elements with `"type": "asn.1"` are encoded using ASN.1 DER/BER rules
via the BouncyCastle library.

The only attribute required by every ASN.1 element is `tag`.

```json
{
  "type": "asn.1",
  "attributes": { "tag": "<tag-name>" },
  "body": <value>
}
```

← [Back to Format overview](README.md)

---

## Table of contents

- [Structural tags](#structural-tags)
  - [sequence](#sequence)
  - [set](#set)
- [Primitive tags](#primitive-tags)
  - [object_identifier](#object_identifier)
  - [boolean](#boolean)
  - [integer](#integer)
  - [octet_string](#octet_string)
  - [bit_string](#bit_string)
  - [null](#null)
- [String tags](#string-tags)
  - [utf8_string](#utf8_string)
  - [printable_string](#printable_string)
  - [ia5_string](#ia5_string)
  - [visible_string](#visible_string)
  - [bmp_string](#bmp_string)
- [Time tags](#time-tags)
  - [generalized_time](#generalized_time)
  - [utc_time](#utc_time)
- [Context tagging](#context-tagging)
  - [tagged_object](#tagged_object)

---

## Structural tags

### sequence

Encodes a DER SEQUENCE. The body must be an **array of child elements**;
each child is encoded in order and the results are wrapped in a SEQUENCE
TLV envelope.

```json
{
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
```

---

### set

Identical to `sequence` but encodes a DER SET. The body is an **array of
child elements**.

```json
{
  "type": "asn.1",
  "attributes": { "tag": "set" },
  "body": [
    { "type": "asn.1", "attributes": { "tag": "sequence" }, "body": [...] }
  ]
}
```

---

## Primitive tags

### object_identifier

Encodes a dotted-decimal OID string.

| Body | Example |
|------|---------|
| Dotted-decimal string literal | `"2.5.4.3"` |
| Placeholder resolving to a dotted-decimal string | `"${policy_oid}"` |

```json
{ "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.4.3" }
```

---

### boolean

Encodes a DER BOOLEAN (`0xff` for true, `0x00` for false).

| Body | Example |
|------|---------|
| JSON boolean | `true` / `false` |
| String literal | `"true"` / `"false"` |
| Placeholder resolving to a boolean | `"${is_critical}"` |

```json
{
  "type": "asn.1",
  "optional": true,
  "attributes": { "tag": "boolean" },
  "body": "${key_usage_critical}"
}
```

`optional: true` is common here — if the value is `null` (not supplied),
the element is omitted instead of defaulting to false.

---

### integer

Encodes a DER INTEGER.

| Body | Example |
|------|---------|
| JSON integer | `42` |
| String integer | `"42"` |
| Placeholder resolving to a number | `"${serial_number}"` |

```json
{ "type": "asn.1", "attributes": { "tag": "integer" }, "body": "${serial_number}" }
```

---

### octet_string

Encodes a DER OCTET STRING. The body must produce **binary** output — it
is almost always a nested element rather than a plain string.

```json
{
  "type": "asn.1",
  "attributes": { "tag": "octet_string" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "sequence" },
    "body": [...]
  }
}
```

For wrapping a hash digest or raw bytes see `sha1` and `hex_to_bin` in
[System elements](system.md).

---

### bit_string

Encodes a DER BIT STRING. The body must produce **binary** output —
typically the output of a `bit_map` system element.

```json
{
  "type": "asn.1",
  "attributes": { "tag": "bit_string" },
  "body": {
    "type": "system",
    "attributes": { "kind": "bit_map", "byte_order": "little_endian", "bit_order": "lsb_first" },
    "body": [
      "${key_usage.digital_signature}",
      "${key_usage.key_encipherment}"
    ]
  }
}
```

---

### null

Encodes a DER NULL (`0x05 0x00`). No body is required.

```json
{ "type": "asn.1", "attributes": { "tag": "null" } }
```

---

## String tags

All string tags accept the same body forms:

| Body | Example |
|------|---------|
| String literal | `"Alice"` |
| Placeholder resolving to a string | `"${cn}"` |

### utf8_string

Encodes a DER UTF8String. Accepts the full Unicode character set.
The most common choice for human-readable fields.

```json
{ "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${common_name}" }
```

---

### printable_string

Encodes a DER PrintableString. The value must contain only printable
ASCII characters (letters, digits, space, and `' ( ) + , - . / : = ?`).

```json
{ "type": "asn.1", "attributes": { "tag": "printable_string" }, "body": "${country}" }
```

---

### ia5_string

Encodes a DER IA5String (7-bit ASCII). Commonly used for email addresses
and URIs in X.509 extensions.

```json
{ "type": "asn.1", "attributes": { "tag": "ia5_string" }, "body": "${email}" }
```

---

### visible_string

Encodes a DER VisibleString (printable ASCII, subset of IA5String).

```json
{ "type": "asn.1", "attributes": { "tag": "visible_string" }, "body": "${hostname}" }
```

---

### bmp_string

Encodes a DER BMPString (UCS-2 / UTF-16 Big Endian). Used in some
legacy PKCS structures.

```json
{ "type": "asn.1", "attributes": { "tag": "bmp_string" }, "body": "${friendly_name}" }
```

---

## Time tags

Both time tags accept a datetime string in the format
`yyyy-MM-dd'T'HH:mm:ss'Z'`.

The correct tag to use depends on the year: RFC 5280 requires UTCTime for
dates before 2050 and GeneralizedTime for 2050 and later. Use a
`condition` system element to select the right one automatically — see
[System elements → condition](system.md#condition).

### generalized_time

Encodes a DER GeneralizedTime. Required for dates ≥ 2050.

```json
{ "type": "asn.1", "attributes": { "tag": "generalized_time" }, "body": "${expires_on}" }
```

---

### utc_time

Encodes a DER UTCTime. Used for dates before 2050 (two-digit year).

```json
{ "type": "asn.1", "attributes": { "tag": "utc_time" }, "body": "${expires_on}" }
```

---

## Context tagging

### tagged_object

Wraps its body in an ASN.1 context tag `[n]`, supporting both IMPLICIT
and EXPLICIT tagging as required by many X.509 / CMS structures.

**Additional attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `tag` | ✅ | `"tagged_object"` |
| `implicit` | ❌ | Context tag number for IMPLICIT tagging, e.g. `0` |
| `explicit` | ❌ | Context tag number for EXPLICIT tagging, e.g. `3` |

Provide exactly one of `implicit` or `explicit`.

```json
{
  "type": "asn.1",
  "attributes": { "tag": "tagged_object", "implicit": 0 },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${subject_alt_name}"
  }
}
```
