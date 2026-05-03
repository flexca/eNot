# System `group` element

← [Back to System elements](index.md)

---

## Description

The `group` element is a pure scoping wrapper. It steps the serialization context **one level deeper** into a named key of the current params map before processing its `body`, then steps back after. It produces no additional output wrapping — the bytes it emits are exactly the bytes produced by its body.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"group"` |
| `group_name` | ✅ | text | Key to step into in the current params map. Must contain only letters, digits, or underscores (`my_group` is valid; `my-group` or `my group` are not). |

---

## Example usage

### Basic example

Consider encoding a Subject Alternative Name extension where the SAN-specific params are nested under a `"san"` key to keep the top-level params map clean:

```yaml
{
  "san_critical": false,
  "san": {
    "dns_name": ["example.com", "www.example.com"]
  }
}
```

Without the `group` element, placeholders inside the body would need to resolve `dns_name` from the root params map. With `group`, they resolve from `san` instead.

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.17"
  - type: asn.1
    optional: true
    attributes:
      tag: boolean
    body: "${san_critical}"
  - type: asn.1
    attributes:
      tag: octet_string
    body:
      type: asn.1
      attributes:
        tag: sequence
      body:
        type: system
        attributes:
          kind: group
          group_name: san
        body:
          - type: system
            attributes:
              kind: reference
              reference_type: san_types
              reference_identifier: dns
```

**params.yaml**
```yaml
san_critical: false
san:
  dns_name:
    - example.com
    - www.example.com
```

Inside the `group` body the active params map is `{ "dns_name": [...] }`. Placeholders and nested elements (loops, references, conditions) all resolve against that scope.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.17"
    },
    {
      "type": "asn.1",
      "optional": true,
      "attributes": { "tag": "boolean" },
      "body": "${san_critical}"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "sequence" },
        "body": {
          "type": "system",
          "attributes": {
            "kind": "group",
            "group_name": "san"
          },
          "body": [
            {
              "type": "system",
              "attributes": {
                "kind": "reference",
                "reference_type": "san_types",
                "reference_identifier": "dns"
              }
            }
          ]
        }
      }
    }
  ]
}
```

**params.json**
```json
{
  "san_critical": false,
  "san": {
    "dns_name": ["example.com", "www.example.com"]
  }
}
```

---

## Scope rules

- The `group` element resolves `group_name` relative to the **current** context scope. If serialization is already inside a `loop` iteration, the group steps into a key inside that iteration's map.
- After the body finishes serializing, the context steps back to the outer scope automatically, even if the body throws an error.
- Placeholders in `group`'s own `attributes` (i.e. `group_name` itself) are resolved **before** the scope step, so `group_name` always refers to a key in the outer scope.

---

## Comparison with `loop`

| | `group` | `loop` |
|---|---------|--------|
| Params value type | Object (map) | Array |
| Iterations | Always 1 | One per array item |
| Purpose | Namespace / scope isolation | Repeated encoding |

Use `group` when the params for a block of elements are packaged as a sub-object. Use `loop` when they are packaged as an array of items.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| `group_name` key is absent from the current params map | Serialization fails with a descriptive error message. |
| The value at `group_name` is not a map | Serialization fails with a type error. |
| `group_name` contains invalid characters | Validation fails before serialization begins. |
