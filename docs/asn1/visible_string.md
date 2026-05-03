# ASN.1 `visible_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER VisibleString (also called ISO646String). Accepts printable ASCII characters — code points 32–126 (space through tilde). Control characters and non-ASCII characters are not permitted.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"visible_string"` |
| `min_length` | ❌ | integer | Minimum number of characters. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum number of characters. Must be non-negative and ≥ `min_length` if both are set. Validated at serialization time. |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of the listed strings. Values must be unique within the list. |

---

## Example usage

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: visible_string
body: "${hostname}"
```

**params.yaml**
```yaml
hostname: server-01.example.com
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "visible_string" },
  "body": "${hostname}"
}
```

**params.json**
```json
{ "hostname": "server-01.example.com" }
```

---

## Notes

- VisibleString is a strict subset of [`ia5_string`](ia5_string.md): it excludes control characters (code points 0–31 and 127).
- For fields where only letters, digits, and the specific symbols `' ( ) + , - . / : = ?` are needed, [`printable_string`](printable_string.md) provides a narrower character set.
- For Unicode content, use [`utf8_string`](utf8_string.md).

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Value contains a character outside code points 32–126 | Serialization fails with a character-set violation error. |
| Character count < `min_length` | Serialization fails with a length error. |
| Character count > `max_length` | Serialization fails with a length error. |
| Value not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `min_length` > `max_length` | Template validation fails. |
