# System `base64_to_bin` element

← [Back to System elements](index.md)

---

## Description

The `base64_to_bin` element decodes a Base64-encoded text string into raw binary bytes. Its body must resolve to exactly one text value. The output is a single binary value containing the decoded bytes.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"base64_to_bin"` |

---

## Example usage

### Basic example

Use `base64_to_bin` when a Base64-encoded payload parameter needs to be supplied as raw binary content to a parent element. For example, encoding a BER-TLV leaf element whose value comes from a Base64 parameter:

**template.yaml**
```yaml
type: ber-tlv
attributes:
  tag: "04"
body:
  type: system
  attributes:
    kind: base64_to_bin
  body: "${payload_base64}"
```

**params.yaml**
```yaml
payload_base64: "3q2+7w=="
```

The placeholder `${payload_base64}` resolves to the text `"3q2+7w=="`. The `base64_to_bin` element decodes it to the four binary bytes `0xDE 0xAD 0xBE 0xEF`, which the parent BER-TLV element encodes as its value.

**template.json**
```json
{
  "type": "ber-tlv",
  "attributes": { "tag": "04" },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "base64_to_bin"
    },
    "body": "${payload_base64}"
  }
}
```

**params.json**
```json
{
  "payload_base64": "3q2+7w=="
}
```

---

## Optional behaviour

When `optional: true` is set on the `base64_to_bin` element and the body resolves to an empty list (e.g. the placeholder uses an absent optional field), the element emits no output and is silently skipped by the parent.

```yaml
type: system
optional: true
attributes:
  kind: base64_to_bin
body: "${optional_base64_field}"
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body resolves to more than one value | Serialization fails — exactly one input is required. |
| Body value is not a text (string) type | Serialization fails with a type error. |
| Body text is not valid Base64 | Serialization fails with a parse error describing the invalid input. |
| Body is empty and `optional` is not `true` | Serialization fails because a required value is missing. |

---

## Notes

- Uses standard (RFC 4648 §4) Base64 decoding, including padding (`=`).
- The output can be used directly as the body of a `bin_to_base64` element to round-trip binary data through a text layer.
