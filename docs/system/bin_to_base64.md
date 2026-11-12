# System `bin_to_base64` element

← [Back to System elements](index.md)

---

## Description

The `bin_to_base64` element is the inverse of [`base64_to_bin`](base64_to_bin.md). It converts a single binary value into a Base64-encoded text string. Its body must resolve to exactly one binary value. The output is a single text value.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"bin_to_base64"` |

---

## Example usage

### Basic example

Use `bin_to_base64` when a parent element expects a text (string) value but the source data is binary. For example, producing a Base64-encoded fingerprint field from raw binary key material:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: utf8_string
body:
  type: system
  attributes:
    kind: bin_to_base64
  body: "${key_bytes}"
```

**params.yaml**
```yaml
key_bytes: !!binary "3q2+7w=="
```

The binary value `{0xDE, 0xAD, 0xBE, 0xEF}` is converted to the text string `"3q2+7w=="`, which the parent UTF-8 string element encodes as its value.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "bin_to_base64"
    },
    "body": "${key_bytes}"
  }
}
```

**params.json**
```json
{
  "key_bytes": "3q2+7w=="
}
```

> **Note:** When passing binary values through JSON params, encode them as Base64 strings; the engine decodes them to raw bytes before handing them to the template.

---

## Optional behaviour

When `optional: true` is set on the `bin_to_base64` element and the body resolves to an empty list (e.g. the placeholder uses an absent optional field), the element emits no output and is silently skipped by the parent.

```yaml
type: system
optional: true
attributes:
  kind: bin_to_base64
body: "${optional_binary_field}"
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body resolves to more than one value | Serialization fails — exactly one input is required. |
| Body value is not a binary-compatible type | Serialization fails with a type error. |
| Body is empty and `optional` is not `true` | Serialization fails because a required value is missing. |

---

## Notes

- Uses standard (RFC 4648 §4) Base64 encoding, including padding (`=`).
- An empty binary input (zero bytes) produces the empty string `""`.
- The output can be used directly as the body of a `base64_to_bin` element to round-trip binary data through a text layer.
