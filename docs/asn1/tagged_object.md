# ASN.1 `tagged_object` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Wraps its body in an ASN.1 context-specific tag `[n]`, supporting both IMPLICIT and EXPLICIT tagging as required by many X.509, CMS, and PKIX structures.

Exactly one of `implicit` or `explicit` must be provided.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"tagged_object"` |
| `implicit` | ❌ | integer | Context tag number for IMPLICIT tagging. Must be a positive integer (≥ 1). The body's own tag is replaced by this context tag. Exactly one of `implicit` or `explicit` must be present. |
| `explicit` | ❌ | integer | Context tag number for EXPLICIT tagging. Must be a positive integer (≥ 1). The body is wrapped in an outer context-specific envelope with this tag number. Exactly one of `implicit` or `explicit` must be present. |

---

## IMPLICIT vs. EXPLICIT tagging

- **IMPLICIT** `[n]`: The body's universal tag is replaced by the context tag `[n]`. The body's own TLV header is not included — only its value bytes are wrapped with the new tag. Used when the standard says `[n] IMPLICIT`.
- **EXPLICIT** `[n]`: The body is fully encoded first, then that encoding is wrapped inside an outer `[n]` envelope. Used when the standard says `[n] EXPLICIT` or when the base type is `CHOICE` or `ANY`.

---

## Example usage

### SAN dNSName — IMPLICIT tagging

Subject Alternative Name dNSName entries (RFC 5280 §4.2.1.6) are encoded as `[2] IMPLICIT IA5String`:

**template.yaml**
```yaml
type: asn.1
optional: true
attributes:
  tag: tagged_object
  implicit: 2
body:
  type: asn.1
  optional: true
  attributes:
    tag: ia5_string
  body: "${value}"
```

**params.yaml**
```yaml
value: example.com
```

The IA5String tag `0x16` is replaced by the context tag `[2]` (`0x82`), and the DNS name bytes are wrapped directly.

**template.json**
```json
{
  "type": "asn.1",
  "optional": true,
  "attributes": {
    "tag": "tagged_object",
    "implicit": 2
  },
  "body": {
    "type": "asn.1",
    "optional": true,
    "attributes": { "tag": "ia5_string" },
    "body": "${value}"
  }
}
```

**params.json**
```json
{ "value": "example.com" }
```

---

### EXPLICIT tagging example

When the body is a CHOICE or ANY type, EXPLICIT tagging is required to preserve the inner type information:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: tagged_object
  explicit: 3
body:
  type: asn.1
  attributes:
    tag: utf8_string
  body: "${subject_alt_name}"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": {
    "tag": "tagged_object",
    "explicit": 3
  },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${subject_alt_name}"
  }
}
```

The UTF8String is encoded first, then wrapped in the `[3]` context-specific envelope.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Both `implicit` and `explicit` are present | Serialization fails — exactly one is allowed. |
| Neither `implicit` nor `explicit` is present | Template validation and serialization fail. |
| Tag number is zero or negative | Serialization fails — value must be a positive integer. |
| Body produces more than one element | Serialization fails — exactly one child is required. |
| Body is not an ASN.1 element | Serialization fails with a type error. |

---

## Notes

- Tag numbers are context-specific class tags. Class and constructed/primitive bits are set automatically based on the tagging mode and body type.
- For `tagged_object` with `implicit`, the body element must have a definite DER encoding. The body's tag byte is discarded and replaced by the context tag.
