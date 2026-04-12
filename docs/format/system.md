# System elements

Elements with `"type": "system"` provide control-flow and binary
transformation capabilities. The specific behaviour is selected by the
mandatory `kind` attribute.

```json
{
  "type": "system",
  "attributes": { "kind": "<kind-name>", ... },
  "body": <value>
}
```

← [Back to Format overview](README.md)

---

## Table of contents

- [loop](#loop)
- [condition](#condition)
- [group](#group)
- [reference](#reference)
- [bit_map](#bit_map)
- [sha1](#sha1)
- [hex_to_bin](#hex_to_bin)
- [bin_to_hex](#bin_to_hex)

---

## loop

Repeats its `body` once for every item in a named array parameter,
producing a concatenated binary output.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"loop"` |
| `items_name` | ✅ | Key in the params map whose value is the array to iterate |
| `min_items` | ❌ | Minimum number of items (validation, default: no minimum) |
| `max_items` | ❌ | Maximum number of items (validation, default: no maximum) |

Inside the loop body, placeholders are resolved from the **current
iteration's map entry**. See
[Scope and nesting](README.md#scope-and-nesting) for details.

**Example — encode a list of Organizational Unit RDNs:**

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

**Parameters:**

```json
{
  "organizational_units": [
    { "unit": "Engineering" },
    { "unit": "Security" }
  ]
}
```

The engine writes two SET elements, one per list entry.

---

## condition

Serializes its `body` only when the `expression` evaluates to `true`.
If the condition is `false`, the element produces **no bytes** and no
error is raised.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"condition"` |
| `expression` | ✅ | Boolean expression string. See [Expression syntax](expressions.md). |

**Example — RFC 5280: pick UTCTime vs GeneralizedTime based on the year:**

```json
[
  {
    "type": "system",
    "attributes": {
      "kind": "condition",
      "expression": "${valid_from} < '2050-01-01T00:00:00Z'"
    },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "utc_time" },
      "body": "${valid_from}"
    }
  },
  {
    "type": "system",
    "attributes": {
      "kind": "condition",
      "expression": "${valid_from} >= '2050-01-01T00:00:00Z'"
    },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "generalized_time" },
      "body": "${valid_from}"
    }
  }
]
```

**Example — optional extension, only include if SAN list is non-empty:**

```json
{
  "type": "system",
  "attributes": {
    "kind": "condition",
    "expression": "length(${san_entries}) > 0"
  },
  "body": { ... }
}
```

See [Expression syntax](expressions.md) for the full expression language
reference.

---

## group

Names a sub-tree of elements so it can be inserted elsewhere via
`reference`. The `body` is serialized in place and its output is also
stored under the given name for later reuse.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"group"` |
| `name` | ✅ | Unique name for this group within the template |

```json
{
  "type": "system",
  "attributes": { "kind": "group", "name": "subject_public_key_info" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "sequence" },
    "body": [...]
  }
}
```

---

## reference

Inserts the binary output previously produced by a `group` element at
the current position. Used when the same encoded structure must appear
at multiple points — for example, a public key that is both part of the
certificate body and the source of the Subject Key Identifier digest.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"reference"` |

The `body` of a `reference` element names the group to insert:

```json
{
  "type": "system",
  "attributes": { "kind": "reference" },
  "body": "subject_public_key_info"
}
```

The referenced group must have been serialized earlier in the same
serialization run.

---

## bit_map

Converts an ordered list of boolean values into a compact bit-field byte
array. Each boolean in the `body` array maps to one bit in declaration
order.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"bit_map"` |
| `byte_order` | ✅ | `"big_endian"` or `"little_endian"` |
| `bit_order` | ✅ | `"msb_first"` or `"lsb_first"` |

**Body:** a JSON array of boolean placeholders or literals.

The output is raw binary and is almost always wrapped in a
`bit_string` ASN.1 element.

**Example — X.509 Key Usage extension:**

```json
{
  "type": "asn.1",
  "attributes": { "tag": "bit_string" },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "bit_map",
      "byte_order": "little_endian",
      "bit_order": "lsb_first"
    },
    "body": [
      "${key_usage.digital_signature}",
      "${key_usage.non_repudiation}",
      "${key_usage.key_encipherment}",
      "${key_usage.data_encipherment}",
      "${key_usage.key_agreement}",
      "${key_usage.key_cert_sign}",
      "${key_usage.crl_sign}",
      "${key_usage.encipher_only}",
      "${key_usage.decipher_only}"
    ]
  }
}
```

**Parameters:**

```json
{
  "key_usage": {
    "digital_signature": true,
    "key_encipherment": true,
    "non_repudiation": false,
    "data_encipherment": false,
    "key_agreement": false,
    "key_cert_sign": false,
    "crl_sign": false,
    "encipher_only": false,
    "decipher_only": false
  }
}
```

---

## sha1

Computes the **SHA-1 digest** of the binary output produced by its
`body` and returns the 20-byte digest as raw binary.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"sha1"` |

Commonly used for Subject Key Identifier and Authority Key Identifier
extensions in X.509 certificates (RFC 5280 §4.2.1.2).

**Example — Authority Key Identifier (SHA-1 of issuer public key):**

```json
{
  "type": "asn.1",
  "attributes": { "tag": "octet_string" },
  "body": {
    "type": "system",
    "attributes": { "kind": "sha1" },
    "body": {
      "type": "system",
      "attributes": { "kind": "hex_to_bin" },
      "body": "${issuer_public_key_hex}"
    }
  }
}
```

Reading inside-out:
1. `hex_to_bin` decodes the hex string into raw bytes.
2. `sha1` hashes those bytes, producing a 20-byte digest.
3. `octet_string` wraps the digest in an ASN.1 OCTET STRING.

---

## hex_to_bin

Decodes a **hexadecimal string** into raw bytes.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"hex_to_bin"` |

**Body:** a hex-encoded string value or placeholder (upper or lower
case, no separators).

```json
{
  "type": "system",
  "attributes": { "kind": "hex_to_bin" },
  "body": "${issuer_public_key_hex}"
}
```

---

## bin_to_hex

Encodes the **raw binary output** of its body as a hexadecimal string.
The result is a text value, not binary — useful for intermediate
transformations or when a downstream element expects a hex string.

**Attributes:**

| Attribute | Required | Description |
|-----------|----------|-------------|
| `kind` | ✅ | `"bin_to_hex"` |

```json
{
  "type": "system",
  "attributes": { "kind": "bin_to_hex" },
  "body": {
    "type": "system",
    "attributes": { "kind": "sha1" },
    "body": { ... }
  }
}
```
