# System `random_generator` element

← [Back to System elements](index.md)

---

## Description

The `random_generator` element produces a random alphanumeric text value using a cryptographically secure random source (`java.security.SecureRandom`). It takes no body — the generated value is driven entirely by the `random_string_length` attribute. The output is a single text value of exactly the requested length.

| Attribute | Required | Type | Description |
|-----------|----------|------|--------------|
| `kind` | ✅ | text | Must be `"random_generator"` |
| `random_string_length` | ✅ | integer | Number of characters to generate. Must be greater than `0` and less than or equal to `4096`. |

The generated characters are drawn uniformly from the alphanumeric alphabet `0-9`, `a-z`, `A-Z` (62 characters total).

---

## Example usage

### Basic example

Use `random_generator` when a template field must carry a freshly generated value rather than one supplied via params — for example, a random transaction identifier embedded as a UTF-8 string:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: utf8_string
body:
  type: system
  attributes:
    kind: random_generator
    random_string_length: 16
```

No params are required — `random_generator` ignores the params scope entirely and generates its value directly.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "utf8_string" },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "random_generator",
      "random_string_length": 16
    }
  }
}
```

Each serialization call produces a different 16-character alphanumeric string, e.g. `"aZ3fQ0mK9pLxT2cR"`.

---

## Notes

- `random_generator` has no body — any `body` provided in the template is ignored by the element's value specification (`EMPTY` consume type) and must be `null` or blank if present.
- The random source is `java.security.SecureRandom`, suitable for generating identifiers, nonces, or tokens where unpredictability matters.
- Because the output changes on every invocation, `random_generator` is not deterministic and cannot be used to reproduce a fixed test vector; use a literal or a param placeholder for deterministic values instead.
- The output is always `TEXT`. Wrap it with an element able to consume text (such as an `asn.1` string tag, or [`hex_to_bin`](hex_to_bin.md) / [`base64_to_bin`](base64_to_bin.md) if the random text itself is hex/Base64) to turn it into binary output.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| `random_string_length` attribute is absent | Validation fails before serialization begins. |
| `random_string_length` is not an integer | Validation fails with a type error. |
| `random_string_length` is less than or equal to `0` | Validation fails — length must be greater than zero. |
| `random_string_length` is greater than `4096` | Validation fails — length must not exceed the absolute maximum of `4096`. |
