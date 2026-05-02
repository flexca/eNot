# ASN.1 `sequence` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER SEQUENCE. The body must be an **array of child elements**. Each child is encoded in declaration order and the results are wrapped in a SEQUENCE TLV envelope.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"sequence"` |

---

## Example usage

### Subject Common Name (RFC 5280)

A Distinguished Name Relative field — `SET { SEQUENCE { OID, UTF8String } }`:

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
      body: "2.5.4.3"
    - type: asn.1
      attributes:
        tag: utf8_string
      body: "${common_name}"
```

**params.yaml**
```yaml
common_name: Alice
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
        "body": "2.5.4.3"
      },
      {
        "type": "asn.1",
        "attributes": { "tag": "utf8_string" },
        "body": "${common_name}"
      }
    ]
  }
}
```

**params.json**
```json
{ "common_name": "Alice" }
```

---

## Notes

- Children are encoded strictly in the order they appear in the array. DER requires a fixed encoding order; for `SET` elements where sorting is needed, use the [`set`](set.md) tag.
- The body of a `sequence` element is **always** an array, even when there is only one child.
- When the body array is produced by a system element (e.g. `loop`), pass a single system element as the body — the system element's output (multiple encoded children) will be collected into the SEQUENCE.
