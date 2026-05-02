# System `hex_to_bin` element

← [Back to System elements](index.md)

---

## Description

The `hex_to_bin` element decodes a hexadecimal text string into raw binary bytes. Its body must resolve to exactly one text value. The output is a single binary value containing the decoded bytes.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"hex_to_bin"` |

---

## Example usage

### Basic example

The most common use case is supplying a raw binary payload to a parent element that expects binary content. For example, encoding a BER-TLV leaf element whose value comes from a hex parameter:

**template.yaml**
```yaml
type: ber-tlv
attributes:
  tag: "04"
body:
  type: system
  attributes:
    kind: hex_to_bin
  body: "${payload_hex}"
```

**params.yaml**
```yaml
payload_hex: "deadbeef"
```

The placeholder `${payload_hex}` resolves to the text `"deadbeef"`. The `hex_to_bin` element decodes it to the four binary bytes `0xDE 0xAD 0xBE 0xEF`, which the parent BER-TLV element encodes as its value.

**template.json**
```json
{
  "type": "ber-tlv",
  "attributes": { "tag": "04" },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "hex_to_bin"
    },
    "body": "${payload_hex}"
  }
}
```

**params.json**
```json
{
  "payload_hex": "deadbeef"
}
```

---

### Chaining with `sha1`

`hex_to_bin` is often chained with other system elements when a hex parameter needs further processing. The [Authority Key Identifier](https://www.rfc-editor.org/rfc/rfc5280#section-4.2.1.1) extension (RFC 5280 §4.2.1.1) stores the SHA-1 hash of the issuer's public key. Using `hex_to_bin` together with `sha1`:

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

**params.yaml**
```yaml
issuer_public_key_hex: "3082..."
```

The hex string is first decoded to binary by `hex_to_bin`, and the resulting bytes are then hashed by `sha1`.

---

## Optional behaviour

When `optional: true` is set on the `hex_to_bin` element and the body resolves to an empty list (e.g. the placeholder uses an absent optional field), the element emits no output and is silently skipped by the parent.

```yaml
type: system
optional: true
attributes:
  kind: hex_to_bin
body: "${optional_hex_field}"
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body resolves to more than one value | Serialization fails — exactly one input is required. |
| Body value is not a text (string) type | Serialization fails with a type error. |
| Body text is not valid hexadecimal | Serialization fails with a parse error describing the invalid input. |
| Body is empty and `optional` is not `true` | Serialization fails because a required value is missing. |

---

## Notes

- Hex input is case-insensitive: `"DEADBEEF"` and `"deadbeef"` produce the same bytes.
- An empty string `""` decodes to a zero-length binary value (empty bytes).
