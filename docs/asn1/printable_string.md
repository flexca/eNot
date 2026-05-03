# ASN.1 `printable_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER PrintableString. The value must contain only printable ASCII characters: letters (`A–Z`, `a–z`), digits (`0–9`), space, and the symbols `' ( ) + , - . / : = ?`. Characters outside this set are rejected at serialization time.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"printable_string"` |
| `min_length` | ❌ | integer | Minimum number of characters. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum number of characters. Must be non-negative and ≥ `min_length` if both are set. Validated at serialization time. |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of the listed strings. Values must be unique within the list. |

---

## Example usage

### Country code (2-letter ISO 3166-1)

`countryName` (OID `2.5.4.6`) in X.509 DNs is typically encoded as a PrintableString:

**template.yaml**
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
      body: "2.5.4.6"
    - type: asn.1
      attributes:
        tag: printable_string
        min_length: 2
        max_length: 2
      body: "${country}"
```

**params.yaml**
```yaml
country: US
```

**template.json**
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
        "body": "2.5.4.6"
      },
      {
        "type": "asn.1",
        "attributes": {
          "tag": "printable_string",
          "min_length": 2,
          "max_length": 2
        },
        "body": "${country}"
      }
    ]
  }
}
```

**params.json**
```json
{ "country": "US" }
```

---

## Notes

- When a field may contain characters outside the PrintableString alphabet (e.g. accented letters, `@`, `#`), use [`utf8_string`](utf8_string.md) instead. RFC 5280 allows either for most DN attribute types.
- The character check is performed at serialization time after the placeholder is resolved, so a template-level `allowed_values` list is also a good way to ensure only valid country codes are accepted.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Value contains a character not in the PrintableString alphabet | Serialization fails with a character-set violation error. |
| Character count < `min_length` | Serialization fails with a length error. |
| Character count > `max_length` | Serialization fails with a length error. |
| Value not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `min_length` > `max_length` | Template validation fails. |
