# ASN.1 `set` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER SET. Identical to [`sequence`](sequence.md) in terms of template syntax — the body is an **array of child elements** — but the output carries the SET universal tag (`0x31`) instead of the SEQUENCE tag (`0x30`).

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"set"` |

---

## Example usage

### Relative Distinguished Name (RFC 5280)

An RDN is encoded as `SET { SEQUENCE { OID, value } }`:

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

- Children are encoded in the order they appear in the array. The engine does **not** automatically sort SET members by their DER encoding — if the target standard requires sorted SET elements (such as a distinguished name with multiple RDNs), arrange them in the template accordingly.
- Like `sequence`, the body is always an array even when there is only one child.
