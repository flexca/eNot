# ASN.1 `integer` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER INTEGER. Accepts `int`, `long`, and `BigInteger` values, so any precision of integer can be represented.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"integer"` |

---

## Example usage

### Serial number

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: integer
body: "${serial_number}"
```

**params.yaml**
```yaml
serial_number: 42
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "integer" },
  "body": "${serial_number}"
}
```

**params.json**
```json
{ "serial_number": 42 }
```

---

### Literal integer

Integers can also be specified as literals directly in the template:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: integer
body: 0
```

---

## Body forms

| Form | Example |
|------|---------|
| JSON integer | `42` |
| JSON string containing an integer | `"42"` |
| Placeholder resolving to an integer | `"${serial_number}"` |

---

## Notes

- Negative integers are supported.
- When a large serial number is passed as a string (e.g. `"12345678901234567890"`), the engine parses it as a `BigInteger`.
- X.509 serial numbers (RFC 5280 §4.1.2.2) must be positive and no longer than 20 octets.
