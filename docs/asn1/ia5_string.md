# ASN.1 `ia5_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER IA5String (International Alphabet 5 — essentially 7-bit ASCII, code points 0–127). Commonly used for email addresses, URIs, and DNS names in X.509 extensions.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"ia5_string"` |
| `min_length` | ❌ | integer | Minimum number of characters. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum number of characters. Must be non-negative and ≥ `min_length` if both are set. Validated at serialization time. |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of the listed strings. Values must be unique within the list. |

---

## Example usage

### DNS Subject Alternative Name

SAN dNSName entries (RFC 5280 §4.2.1.6) are encoded as IMPLICIT `[2]` IA5String:

**template.yaml**
```yaml
type: asn.1
optional: true
attributes:
  tag: tagged_object
  implicit: 2
body:
  type: asn.1
  optional: true
  attributes:
    tag: ia5_string
  body: "${value}"
```

**params.yaml**
```yaml
value: example.com
```

**template.json**
```json
{
  "type": "asn.1",
  "optional": true,
  "attributes": {
    "tag": "tagged_object",
    "implicit": 2
  },
  "body": {
    "type": "asn.1",
    "optional": true,
    "attributes": { "tag": "ia5_string" },
    "body": "${value}"
  }
}
```

**params.json**
```json
{ "value": "example.com" }
```

---

### Email address

`emailAddress` attribute (OID `1.2.840.113549.1.9.1`) in PKCS#9 / legacy DNs:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: ia5_string
body: "${email_address}"
```

**params.yaml**
```yaml
email_address: alice@example.com
```

---

## Notes

- IA5String accepts all 7-bit ASCII characters (0–127), including control characters. If only printable characters are needed, consider [`printable_string`](printable_string.md) or [`visible_string`](visible_string.md).
- For DNS names and URIs, characters are always within the ASCII range so IA5String is the correct choice per RFC 5280.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Value contains a character with code point > 127 | Serialization fails with a character-set violation error. |
| Character count < `min_length` | Serialization fails with a length error. |
| Character count > `max_length` | Serialization fails with a length error. |
| Value not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `min_length` > `max_length` | Template validation fails. |
