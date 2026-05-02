# ASN.1 `generalized_time` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER GeneralizedTime. Required by RFC 5280 for dates on or after 1 January 2050.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"generalized_time"` |

---

## Input format

The body must be an ISO 8601 UTC datetime string:

```
yyyy-MM-dd'T'HH:mm:ss'Z'
```

Example: `"2050-06-01T00:00:00Z"`

---

## Example usage

### X.509 Validity — dates ≥ 2050

Per RFC 5280 §4.1.2.5, certificate validity dates on or after 2050 must use GeneralizedTime. Use a [`condition`](../system/condition.md) system element to select the right time tag automatically:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: generalized_time
      body: "${valid_from}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes:
        tag: utc_time
      body: "${valid_from}"
```

**params.yaml**
```yaml
valid_from: "2050-06-01T00:00:00Z"
expires_on: "2051-06-01T00:00:00Z"
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
        "expression": "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "generalized_time" },
        "body": "${valid_from}"
      }
    },
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
    }
  ]
}
```

**params.json**
```json
{
  "valid_from": "2050-06-01T00:00:00Z",
  "expires_on": "2051-06-01T00:00:00Z"
}
```

---

## Notes

- The DER encoding of GeneralizedTime uses a 4-digit year (`YYYYMMDDHHMMSSZ`), making it unambiguous for dates past 2049.
- For dates before 2050, use [`utc_time`](utc_time.md) (RFC 5280 requirement).
- See the [condition](../system/condition.md) page for the full `date_time()` expression syntax.
