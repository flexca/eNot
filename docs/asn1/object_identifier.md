# ASN.1 `object_identifier` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a dotted-decimal OID string as a DER OBJECT IDENTIFIER.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"object_identifier"` |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of these OID strings. Useful for asserting that a parameter contains an expected OID. All values must be valid dotted-decimal OIDs and must be unique within the list. |

---

## Example usage

### Literal OID

The most common use — including a well-known OID directly in the template:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: object_identifier
body: "2.5.4.3"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "object_identifier" },
  "body": "2.5.4.3"
}
```

---

### Parameterised OID

When the OID is supplied at runtime (e.g. a policy OID chosen per certificate):

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: object_identifier
body: "${policy_oid}"
```

**params.yaml**
```yaml
policy_oid: "2.16.840.1.114412.1.1"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "object_identifier" },
  "body": "${policy_oid}"
}
```

**params.json**
```json
{ "policy_oid": "2.16.840.1.114412.1.1" }
```

---

### Restricting to allowed OIDs

Use `allowed_values` to validate that a caller-supplied OID belongs to a permitted set:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: object_identifier
  allowed_values:
    - "1.2.840.10045.4.3.2"
    - "1.2.840.113549.1.1.11"
body: "${signature_algorithm_oid}"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": {
    "tag": "object_identifier",
    "allowed_values": [
      "1.2.840.10045.4.3.2",
      "1.2.840.113549.1.1.11"
    ]
  },
  "body": "${signature_algorithm_oid}"
}
```

If `${signature_algorithm_oid}` resolves to anything other than one of the two listed OIDs, serialization fails with a validation error.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body value is not a valid dotted-decimal OID | Serialization fails. |
| Body value is not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `allowed_values` contains duplicate OID strings | Template validation fails. |
| `allowed_values` contains a non-OID string | Template validation fails. |
