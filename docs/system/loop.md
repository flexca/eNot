# System `loop` element

← [Back to System elements](index.md)

---

## Description

The `loop` element iterates over a named array in the serialization parameters and executes its `body` once for every item. The results from all iterations are concatenated into a flat sequence and passed to the parent element.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"loop"` |
| `items_name` | ✅ | text | Key in the params map whose value is the array to iterate. Must contain only letters, digits, or underscores (`my_items` is valid; `my-items` or `my items` are not). |
| `min_items` | ❌ | integer | Minimum number of array items required. Validated at serialization time. Must be non-negative. |
| `max_items` | ❌ | integer | Maximum number of array items allowed. Validated at serialization time. Must be non-negative. |
| `uniqueness` | ❌ | text | `"none"` (default) — duplicates are allowed. `"enforce"` — serialization fails if any two iterations have an identical set of parameter values. |

---

## Example usage

### Basic example

Consider encoding a list of Organizational Unit names as part of a Distinguished Name. The ASN.1 structure for one OU entry is:

```asn1
RelativeDistinguishedName ::= SET {
    AttributeTypeAndValue ::= SEQUENCE {
        type   OBJECT IDENTIFIER,   -- 2.5.4.11
        value  UTF8String
    }
}
```

To produce one `SET` per OU from a variable-length list, use `loop`:

**template.yaml**
```yaml
type: system
attributes:
  kind: loop
  items_name: organizational_units
body:
  type: asn.1
  attributes: { tag: set }
  body:
    type: asn.1
    attributes: { tag: sequence }
    body:
      - type: asn.1
        attributes: { tag: object_identifier }
        body: "2.5.4.11"
      - type: asn.1
        attributes: { tag: utf8_string }
        body: "${unit}"
```

**params.yaml**
```yaml
organizational_units:
  - unit: Engineering
  - unit: Security
```

The engine steps into the `organizational_units` array. For each item the placeholder `${unit}` is resolved from that item's keys. The result is two `SET` elements concatenated in sequence.

**template.json**
```json
{
  "type": "system",
  "attributes": {
    "kind": "loop",
    "items_name": "organizational_units"
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "set" },
    "body": {
      "type": "asn.1",
      "attributes": { "tag": "sequence" },
      "body": [
        {
          "type": "asn.1",
          "attributes": { "tag": "object_identifier" },
          "body": "2.5.4.11"
        },
        {
          "type": "asn.1",
          "attributes": { "tag": "utf8_string" },
          "body": "${unit}"
        }
      ]
    }
  }
}
```

**params.json**
```json
{
  "organizational_units": [
    { "unit": "Engineering" },
    { "unit": "Security" }
  ]
}
```

### Placeholder scope and items_name

When the engine enters a loop iteration, the **current item's map becomes the active scope** for placeholder resolution. Outer params defined at a higher level remain accessible as long as they are not shadowed by an item key.

For example, given:

**params.yaml**
```yaml
subject_cn: Alice
organizational_units:
  - unit: Engineering
    location: Berlin
  - unit: Security
    location: London
```

**params.json**
```json
{
  "subject_cn": "Alice",
  "organizational_units": [
    { "unit": "Engineering", "location": "Berlin" },
    { "unit": "Security",    "location": "London" }
  ]
}
```

Inside the loop body:
- `${unit}` and `${location}` resolve from the **current item's keys** and change each iteration.
- `${subject_cn}` resolves from the **outer scope** and remains `"Alice"` throughout.

**Key lookup is case-sensitive.** The value of `items_name` must exactly match the key in your params map. If the template says `items_name: organizational_units` but the params key is `Organizational_Units`, the engine will not find the array — it will not report an error, it will attempt one iteration where all placeholders resolve to `null`. The safest convention is to use lowercase snake_case for all param keys and `items_name` values.

**Empty array vs absent key:**
- If the array exists but is **empty** (`organizational_units: []`), `hasNext()` returns `false` immediately and the loop produces no output. `min_items` is then checked — if set to 1 or more, serialization fails.
- If the key is **absent** from params entirely, the engine attempts one iteration with all placeholders resolving to `null`. Whether this throws or silently produces empty output depends on the body elements and their optionality. To make absence safe, mark the loop element itself as optional (`optional: true`) — an optional loop with an absent key produces no output and throws no error.

### Nested loops

A loop body can itself contain another loop, allowing iteration over nested arrays. The inner loop's `items_name` is resolved relative to the **current outer iteration's item map**.

For example, to encode a list of subjects where each subject has its own list of DNS names:

**params.yaml**
```yaml
subjects:
  - cn: Alice
    dns_names:
      - value: alice.example.com
      - value: alice2.example.com
  - cn: Bob
    dns_names:
      - value: bob.example.com
```

**params.json**
```json
{
  "subjects": [
    {
      "cn": "Alice",
      "dns_names": [
        { "value": "alice.example.com" },
        { "value": "alice2.example.com" }
      ]
    },
    {
      "cn": "Bob",
      "dns_names": [
        { "value": "bob.example.com" }
      ]
    }
  ]
}
```

**template.yaml**
```yaml
type: system
attributes:
  kind: loop
  items_name: subjects
body:
  - type: asn.1
    attributes: { tag: utf8_string }
    body: "${cn}"
  - type: system
    attributes:
      kind: loop
      items_name: dns_names
    body:
      type: asn.1
      attributes: { tag: ia5_string }
      body: "${value}"
```

**template.json**
```json
{
  "type": "system",
  "attributes": { "kind": "loop", "items_name": "subjects" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "utf8_string" },
      "body": "${cn}"
    },
    {
      "type": "system",
      "attributes": { "kind": "loop", "items_name": "dns_names" },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "ia5_string" },
        "body": "${value}"
      }
    }
  ]
}
```

Scoping rules for nested loops:
- The inner loop's `items_name` (`dns_names`) is looked up within the **current outer item** — not at the root of the params tree.
- Inside the inner loop body, `${value}` is resolved from the current inner item.
- `${cn}` from the outer item remains accessible throughout all inner iterations.
- After the inner loop finishes, the outer loop advances to the next item and the inner `dns_names` key is re-resolved from that item.

---

### min_items and max_items

Use these attributes to enforce array length constraints. Validation occurs at serialization time, after iteration completes.

**template.yaml**
```yaml
type: system
attributes:
  kind: loop
  items_name: organizational_units
  min_items: 1
  max_items: 5
body:
  type: asn.1
  attributes: { tag: utf8_string }
  body: "${unit}"
```

**template.json**
```json
{
  "type": "system",
  "attributes": {
    "kind": "loop",
    "items_name": "organizational_units",
    "min_items": 1,
    "max_items": 5
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${unit}"
  }
}
```

| Scenario | Result |
|----------|--------|
| Array has 0 items | Serialization fails — fewer items than `min_items` |
| Array has 3 items | Serialization succeeds |
| Array has 7 items | Serialization fails — more items than `max_items` |

`min_items` must be ≤ `max_items` when both are set; the parser rejects a template where this constraint is violated.

### uniqueness

The `uniqueness` attribute controls whether the loop rejects duplicate entries. When set to `"enforce"`, the engine hashes the entire parameter map of each iteration and compares it against all previous iterations. If any two items produce the same hash, serialization fails.

Uniqueness is evaluated across **the full item map**, not a single field. Two items with different field values are always considered distinct, even if they happen to produce identical binary output.

**Passing case** — all items are distinct:

**template.yaml**
```yaml
type: system
attributes:
  kind: loop
  items_name: organizational_units
  uniqueness: enforce
body:
  type: asn.1
  attributes: { tag: utf8_string }
  body: "${unit}"
```

**params.yaml**
```yaml
organizational_units:
  - unit: Engineering
  - unit: Security
```

**template.json**
```json
{
  "type": "system",
  "attributes": {
    "kind": "loop",
    "items_name": "organizational_units",
    "uniqueness": "enforce"
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${unit}"
  }
}
```

**params.json**
```json
{
  "organizational_units": [
    { "unit": "Engineering" },
    { "unit": "Security" }
  ]
}
```

The two items have different `unit` values → no duplicate detected → serialization succeeds.

**Failing case** — duplicate entry:

**params.yaml**
```yaml
organizational_units:
  - unit: Engineering
  - unit: Engineering
```

**params.json**
```json
{
  "organizational_units": [
    { "unit": "Engineering" },
    { "unit": "Engineering" }
  ]
}
```

Both items are identical maps → the second iteration matches the hash of the first → serialization throws an `EnotSerializationException`.

When `uniqueness` is omitted or set to `"none"`, duplicates are silently allowed.
