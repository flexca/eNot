# ASN.1 `utf8_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER UTF8String. Accepts the full Unicode character set and is the most common choice for human-readable certificate fields.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"utf8_string"` |
| `min_length` | ❌ | integer | Minimum number of characters. Must be non-negative. Validated at serialization time. |
| `max_length` | ❌ | integer | Maximum number of characters. Must be non-negative and ≥ `min_length` if both are set. Validated at serialization time. |
| `allowed_values` | ❌ | array of text | If present, serialization fails unless the resolved body value is one of the listed strings. Values must be unique within the list. |

---

## Example usage

### Common Name in a Distinguished Name

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

### Length constraints

Enforce that the Common Name is between 1 and 64 characters (X.520 upper bound):

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: utf8_string
  min_length: 1
  max_length: 64
body: "${common_name}"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": {
    "tag": "utf8_string",
    "min_length": 1,
    "max_length": 64
  },
  "body": "${common_name}"
}
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Character count < `min_length` | Serialization fails with a length error. |
| Character count > `max_length` | Serialization fails with a length error. |
| Value not in `allowed_values` | Serialization fails with a value-not-allowed error. |
| `min_length` > `max_length` | Template validation fails. |
| `allowed_values` contains duplicates | Template validation fails. |
