# System `ip_address_to_bytes` element

← [Back to System elements](index.md)

---

## Description

The `ip_address_to_bytes` element converts a literal IPv4 or IPv6 address string into raw binary bytes (4 bytes for IPv4, 16 bytes for IPv6). Its body must resolve to exactly one text value. The output is a single binary value containing the address bytes in network byte order.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"ip_address_to_bytes"` |

> **Security note:** the input must be a literal IP address. Hostnames are rejected rather than resolved via DNS, so serialization never triggers a network lookup.

---

## Example usage

### Subject Alternative Name — IP address (RFC 5280 §4.2.1.6)

The [Subject Alternative Name](https://www.rfc-editor.org/rfc/rfc5280#section-4.2.1.6) extension encodes `iPAddress` entries as the raw octets of the address, tagged `[7]` implicit. Combined with `loop`, `ip_address_to_bytes` converts each address parameter to binary:

**template.yaml**
```yaml
type: system
attributes:
  kind: loop
  items_name: ip_address
body:
  type: asn.1
  attributes:
    tag: tagged_object
    implicit: 7
  body:
    type: asn.1
    attributes:
      tag: octet_string
    body:
      type: system
      attributes:
        kind: ip_address_to_bytes
      body: "${address}"
```

**params.yaml**
```yaml
ip_address:
  - address: "192.0.2.1"
  - address: "2001:db8::1"
```

Each `${address}` placeholder is converted to its binary form — 4 bytes for the IPv4 entry, 16 bytes for the IPv6 entry — and wrapped in the `[7]` implicit tagged OCTET STRING.

**template.json**
```json
{
  "type": "system",
  "attributes": { "kind": "loop", "items_name": "ip_address" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "tagged_object", "implicit": 7 },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": {
        "type": "system",
        "attributes": { "kind": "ip_address_to_bytes" },
        "body": "${address}"
      }
    }
  }
}
```

**params.json**
```json
{
  "ip_address": [
    { "address": "192.0.2.1" },
    { "address": "2001:db8::1" }
  ]
}
```

---

## Optional behaviour

When `optional: true` is set on the `ip_address_to_bytes` element and the body resolves to an empty list (e.g. the placeholder uses an absent optional field), the element emits no output and is silently skipped by the parent.

```yaml
type: system
optional: true
attributes:
  kind: ip_address_to_bytes
body: "${optional_ip_field}"
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body resolves to more than one value | Serialization fails — exactly one input is required. |
| Body value is not a text (string) type | Serialization fails with a type error. |
| Body text is not a literal IPv4 or IPv6 address (e.g. a hostname) | Serialization fails with a validation error; no DNS lookup is performed. |
| Body is empty and `optional` is not `true` | Serialization fails because a required value is missing. |

---

## Notes

- IPv4 addresses produce 4 bytes; IPv6 addresses produce 16 bytes.
- Only literal addresses are accepted — hostnames (e.g. `"example.com"`) are rejected rather than resolved.
