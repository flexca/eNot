# ASN.1 `utc_time` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER UTCTime. Required by RFC 5280 for dates before 1 January 2050. Uses a two-digit year, so it can only represent dates within the range 1950–2049.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"utc_time"` |

---

## Input format

The body must be an ISO 8601 UTC datetime string:

```
yyyy-MM-dd'T'HH:mm:ss'Z'
```

Example: `"2026-01-15T10:00:00Z"`

---

## Example usage

### X.509 Validity — dates before 2050

Per RFC 5280 §4.1.2.5, certificate validity dates before 2050 must use UTCTime. Use a [`condition`](../system/condition.md) element to automatically select the correct tag:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: utc_time
      body: "${valid_from}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: generalized_time
      body: "${valid_from}"
```

**params.yaml**
```yaml
valid_from: "2026-01-15T10:00:00Z"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "utc_time" },
        "body": "${valid_from}"
      }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "generalized_time" },
        "body": "${valid_from}"
      }
    }
  ]
}
```

**params.json**
```json
{ "valid_from": "2026-01-15T10:00:00Z" }
```

---

## Notes

- The DER encoding of UTCTime uses a two-digit year (`YYMMDDHHMMSSZ`). RFC 5280 §4.1.2.5 interprets years 00–49 as 2000–2049 and 50–99 as 1950–1999.
- For dates on or after 2050, use [`generalized_time`](generalized_time.md).
- See the [condition](../system/condition.md) page for the full `date_time()` expression syntax and the complete validity-date template pattern.
