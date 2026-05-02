# ASN.1 `boolean` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER BOOLEAN. `true` encodes to `0xFF`, `false` encodes to `0x00`.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"boolean"` |

---

## Example usage

### Optional critical flag (RFC 5280 extensions)

X.509 extensions carry an optional `critical` boolean. When absent the extension is treated as non-critical. Using `optional: true` omits the element when the value is not supplied:

**template.yaml**
```yaml
type: asn.1
optional: true
attributes:
  tag: boolean
body: "${key_usage_critical}"
```

**params.yaml**
```yaml
key_usage_critical: true
```

When `key_usage_critical` is absent from the params map the boolean element is silently skipped.

**template.json**
```json
{
  "type": "asn.1",
  "optional": true,
  "attributes": { "tag": "boolean" },
  "body": "${key_usage_critical}"
}
```

**params.json**
```json
{ "key_usage_critical": true }
```

---

## Body forms

| Form | Example |
|------|---------|
| JSON boolean | `true` / `false` |
| String literal | `"true"` / `"false"` |
| Placeholder resolving to a boolean | `"${is_critical}"` |

---

## Notes

- DER requires that `true` is always encoded as `0xFF` (not `0x01` as allowed by BER).
- `optional: true` is the normal pattern for optional boolean fields — if the parameter is absent the element is omitted entirely rather than defaulting to `false`.
