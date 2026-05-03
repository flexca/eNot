# ASN.1 `bmp_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER BMPString (Basic Multilingual Plane String — UCS-2 / UTF-16 Big Endian). Used in some legacy PKCS structures such as PKCS#12 friendly names.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"bmp_string"` |
| `min_length` | ❌ | integer | Minimum number of characters. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum number of characters. Must be non-negative and ≥ `min_length` if both are set. Validated at serialization time. |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of the listed strings. Values must be unique within the list. |

---

## Example usage

### PKCS#12 friendly name

`friendlyName` (OID `1.2.840.113549.1.9.20`) in PKCS#12 is encoded as a BMPString:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: bmp_string
body: "${friendly_name}"
```

**params.yaml**
```yaml
friendly_name: My Certificate
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "bmp_string" },
  "body": "${friendly_name}"
}
```

**params.json**
```json
{ "friendly_name": "My Certificate" }
```

---

## Notes

- BMPString covers the Basic Multilingual Plane (code points U+0000–U+FFFF) but not supplementary characters (code points above U+FFFF).
- For new structures, prefer [`utf8_string`](utf8_string.md) which covers the full Unicode range and is more compact for ASCII content. BMPString is mainly used when a specific legacy format requires it.
- Each character is stored as 2 bytes in the encoded output, so a string of `n` characters produces `2n` bytes in the value field.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Value contains a character outside the BMP (code point > U+FFFF) | Serialization fails with a character-set violation error. |
| Character count < `min_length` | Serialization fails with a length error. |
| Character count > `max_length` | Serialization fails with a length error. |
| Value not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `min_length` > `max_length` | Template validation fails. |
