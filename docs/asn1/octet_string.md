# ASN.1 `octet_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER OCTET STRING. The body must produce **binary** output. It is almost always a nested element (another ASN.1 element, or a [`sha1`](../system/sha1.md) / [`hex_to_bin`](../system/hex_to_bin.md) system element) rather than a plain string placeholder.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"octet_string"` |
| `min_length` | ❌ | integer | Minimum byte length of the encoded value. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum byte length of the encoded value. Must be non-negative, and ≥ `min_length` if both are set. Validated at serialization time. |

---

## Example usage

### Wrapping an encoded ASN.1 structure

X.509 extensions store their value as an OCTET STRING wrapping another DER-encoded structure (RFC 5280 §4.1):

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: octet_string
body:
  type: asn.1
  attributes:
    tag: sequence
  body:
    - type: asn.1
      attributes:
        tag: object_identifier
      body: "2.5.29.17"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "octet_string" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "sequence" },
    "body": [
      {
        "type": "asn.1",
        "attributes": { "tag": "object_identifier" },
        "body": "2.5.29.17"
      }
    ]
  }
}
```

---

### Wrapping a hash digest

OCTET STRING is also used to carry SHA-1 key identifiers:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: octet_string
body:
  type: system
  attributes:
    kind: sha1
  body:
    type: system
    attributes:
      kind: hex_to_bin
    body: "${issuer_public_key_hex}"
```

**template.json**
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

---

### Length constraints

Enforce that a raw value supplied by the caller is within an expected byte range:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: octet_string
  min_length: 16
  max_length: 32
body:
  type: system
  attributes:
    kind: hex_to_bin
  body: "${key_material_hex}"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": {
    "tag": "octet_string",
    "min_length": 16,
    "max_length": 32
  },
  "body": {
    "type": "system",
    "attributes": { "kind": "hex_to_bin" },
    "body": "${key_material_hex}"
  }
}
```

If the decoded bytes are shorter than 16 or longer than 32, serialization fails with a length error.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body does not produce binary output | Serialization fails with a type error. |
| Encoded byte count < `min_length` | Serialization fails with a length violation error. |
| Encoded byte count > `max_length` | Serialization fails with a length violation error. |
| `min_length` > `max_length` | Template validation fails. |
