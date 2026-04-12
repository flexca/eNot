# eNot Format

An eNot template is a JSON document that describes a binary structure.
At serialization time the engine walks the element tree, resolves
placeholder values from a parameter map, evaluates conditions and loops,
and writes the encoded binary output.

A template file may contain a **single root element** (a JSON object) or
a **list of root elements** (a JSON array of objects).

---

## Table of contents

- [Element structure](#element-structure)
  - [type](#type)
  - [attributes](#attributes)
  - [body](#body)
  - [optional](#optional)
- [Values](#values)
  - [Inline literals](#inline-literals)
  - [Nested elements](#nested-elements)
- [Placeholders](#placeholders)
  - [Syntax](#syntax)
  - [Scope and nesting](#scope-and-nesting)
  - [Global params](#global-params)
  - [Missing values](#missing-values)
- [Type references](#type-references)

---

## Element structure

Every element in an eNot template is a JSON object with four possible
fields:

```
{
  "type":       <string>,    // required — selects the encoding engine
  "attributes": <object>,    // required — type-specific configuration
  "body":       <value>,     // required — the payload to encode
  "optional":   <boolean>    // optional — skip silently when body is null
}
```

### type

`type` selects which encoding engine handles this element. There are
currently two built-in types:

| Value | Description |
|-------|-------------|
| `"asn.1"` | ASN.1 DER/BER encoding. See [ASN.1 elements](asn1.md). |
| `"system"` | Control-flow and transformation. See [System elements](system.md). |

The type system is extensible — new types can be registered via
`EnotRegistry.Builder.withTypeSpecifications(...)`.

```json
{ "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "Hello" }
```

---

### attributes

`attributes` is a JSON object that carries configuration for the chosen
`type`. Each type defines its own required and optional attributes.

For `asn.1`, the only attribute is `tag`:

```json
{ "type": "asn.1", "attributes": { "tag": "sequence" }, "body": [...] }
```

For `system`, the mandatory attribute is `kind`, which selects the
specific system behaviour (`loop`, `condition`, `bit_map`, etc.):

```json
{ "type": "system", "attributes": { "kind": "loop", "items_name": "entries" }, "body": {...} }
```

---

### body

`body` carries the payload that the engine will encode. Its accepted form
depends on `type` and `attributes`:

| Form | Example | Used when |
|------|---------|-----------|
| String literal | `"2.5.4.3"` | Primitive value known at template-write time |
| Placeholder | `"${common_name}"` | Value supplied at serialization time |
| Nested element | `{ "type": "asn.1", ... }` | The encoded output of a child element is the payload |
| Array of elements | `[ { "type": "asn.1", ... }, ... ]` | Structural types like SEQUENCE / SET / loop |

See [Values](#values) and [Placeholders](#placeholders) below for detail.

---

### optional

When `"optional": true` the element is **silently skipped** if its
resolved body value is `null`. No bytes are written and no error is
raised.

```json
{
  "type": "asn.1",
  "optional": true,
  "attributes": { "tag": "boolean" },
  "body": "${key_usage_critical}"
}
```

In the example above, if `key_usage_critical` is absent from the params
map the entire element is omitted from the encoded output. Without
`optional: true` the absence of the value would be an error.

Default: `false`.

---

## Values

### Inline literals

A `body` string that does **not** start with `${` is treated as a
literal value. What it means depends on the target tag:

```json
{ "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.4.3" }
{ "type": "asn.1", "attributes": { "tag": "utf8_string" },       "body": "Alice" }
{ "type": "asn.1", "attributes": { "tag": "boolean" },           "body": "true" }
{ "type": "asn.1", "attributes": { "tag": "integer" },           "body": "42" }
```

Boolean and integer literals are also accepted as native JSON types:

```json
{ "body": true }
{ "body": 42 }
```

---

### Nested elements

When the payload of one element is itself the binary output of another
element, use a nested element object as the body:

```json
{
  "type": "asn.1",
  "attributes": { "tag": "octet_string" },
  "body": {
    "type": "system",
    "attributes": { "kind": "sha1" },
    "body": {
      "type": "system",
      "attributes": { "kind": "hex_to_bin" },
      "body": "${issuer_public_key_hex}"
    }
  }
}
```

Reading from the inside out:

1. `hex_to_bin` decodes the hex string placeholder into raw bytes.
2. `sha1` computes the SHA-1 digest of those bytes.
3. `octet_string` wraps the digest in an ASN.1 OCTET STRING envelope.

---

## Placeholders

### Syntax

A placeholder is a reference to a runtime parameter value. It is always
a string value in the JSON that matches the pattern:

```
"${name}"
```

The name may contain letters, digits, underscores, and dots:

```
"${cn}"
"${subject.common_name}"
"${key_usage.digital_signature}"
```

Dots in the name are **not** path separators — the entire string after
`${` and before `}` is the lookup key in the current scope map.

---

### Scope and nesting

At the root level, placeholders are resolved from the top-level params
map you pass to `EnotSerializer.serialize(...)`.

When the engine enters a `loop` element it pushes a new scope for each
iteration. Inside the loop body, placeholders are resolved from the
**current iteration's map entry**, not the top-level map.

```json
// Template
{
  "type": "system",
  "attributes": { "kind": "loop", "items_name": "sans" },
  "body": {
    "type": "asn.1",
    "attributes": { "tag": "utf8_string" },
    "body": "${value}"          // ← resolved from each item in "sans"
  }
}
```

```java
// Parameters
Map.of("sans", List.of(
    Map.of("value", "alice@example.com"),
    Map.of("value", "bob@example.com")
))
```

The engine writes two UTF8String elements, one per list entry.

---

### Global params

Prefix the placeholder name with `global.` to resolve it from the
**global params map** regardless of the current loop nesting depth:

```
"${global.env}"
"${global.issuer_cn}"
```

Global params are supplied via `SerializationContext.Builder.withGlobalParam(...)` /
`withGlobalParams(...)` and are never affected by loop scope changes.

---

### Missing values

A placeholder whose name is absent from the params map resolves to
`null`.

- If the element is `"optional": true` the element is skipped silently.
- If the element is not optional, serialization fails with an error.
- In a `condition` expression, `null` can be tested explicitly:
  `"is_null(${san})"`.

---

## Type references

| Type | Reference |
|------|-----------|
| `asn.1` | [ASN.1 elements](asn1.md) |
| `system` | [System elements](system.md) |
| Condition expressions | [Expression syntax](expressions.md) |
