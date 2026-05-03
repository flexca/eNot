# ASN.1 `null` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER NULL — the two-byte sequence `0x05 0x00`. No body is required.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"null"` |

---

## Example usage

### Algorithm parameters placeholder

RSA signature algorithm identifiers in X.509 include an explicit NULL as the algorithm parameters field (RFC 5280 §4.1.1.2):

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "1.2.840.113549.1.1.11"
  - type: asn.1
    attributes:
      tag: null
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "1.2.840.113549.1.1.11"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "null" }
    }
  ]
}
```

---

## Notes

- The `body` field may be omitted entirely for a `null` element, or set to `null` — both are equivalent.
- `null` is sometimes wrapped in `optional: true` when the parameters field may or may not be present depending on the algorithm.
