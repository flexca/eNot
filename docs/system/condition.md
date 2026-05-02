# System `condition` element

← [Back to System elements](index.md)

---

## Description

The `condition` element serializes its `body` only when the `expression` attribute evaluates to `true`. When the expression evaluates to `false`, the element produces **no bytes** — it is completely absent from the output, as if it were never in the template.

The expression is evaluated at **serialization time**, after all placeholders have been resolved from the current serialization context.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"condition"` |
| `expression` | ✅ | text | A boolean expression string. Evaluated at serialization time. Must produce a `boolean` result — any other type is a serialization error. |

The `expression` is parsed and structurally validated at **parse time** (placeholder names are checked; the full evaluation only happens during serialization).

---

## Example usage

### Basic example

Encode a `boolean` ASN.1 value only when a certificate is marked as a CA. When `${is_ca}` is `false`, the element produces no output and the parent structure has one fewer child.

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes: { tag: utf8_string }
    body: "${cn}"
  - type: system
    attributes:
      kind: condition
      expression: "${is_ca} == true"
    body:
      type: asn.1
      attributes: { tag: boolean }
      body: "${is_ca}"
```

**params.yaml — condition is true**
```yaml
cn: Alice
is_ca: true
```

**params.yaml — condition is false, CA element absent**
```yaml
cn: Alice
is_ca: false
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "utf8_string" },
      "body": "${cn}"
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "${is_ca} == true"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "boolean" },
        "body": "${is_ca}"
      }
    }
  ]
}
```

**params.json — condition is true**
```json
{ "cn": "Alice", "is_ca": true }
```

**params.json — condition is false, CA element absent**
```json
{ "cn": "Alice", "is_ca": false }
```

### RFC 5280 date selection example

Select `UTCTime` for dates before 2050 and `GeneralizedTime` for dates from 2050 onward, as required by RFC 5280:

**template.yaml**
```yaml
body:
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes: { tag: utc_time }
      body: "${expires_on}"
  - type: system
    attributes:
      kind: condition
      expression: "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"
    body:
      type: asn.1
      attributes: { tag: generalized_time }
      body: "${expires_on}"
```

**params.yaml — uses UTCTime**
```yaml
expires_on: "2030-06-15T00:00:00Z"
```

**params.yaml — uses GeneralizedTime**
```yaml
expires_on: "2055-06-15T00:00:00Z"
```

**template.json**
```json
{
  "body": [
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "utc_time" },
        "body": "${expires_on}"
      }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "condition",
        "expression": "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"
      },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "generalized_time" },
        "body": "${expires_on}"
      }
    }
  ]
}
```

**params.json — uses UTCTime**
```json
{ "expires_on": "2030-06-15T00:00:00Z" }
```

**params.json — uses GeneralizedTime**
```json
{ "expires_on": "2055-06-15T00:00:00Z" }
```

---

## Expression syntax

An expression is a string that evaluates to a boolean. It can contain placeholders, literals, comparison operators, boolean operators, grouping, and built-in functions.

### Primitives

| Syntax | Type | Example |
|--------|------|---------|
| `${name}` | Placeholder — resolved from current scope | `${cn}` |
| `'text'` | String literal (single quotes) | `'Alice'` |
| `42` | Integer literal | `42` |
| `true` / `false` | Boolean literal (case-insensitive) | `true` |
| `null` | Null literal | `null` |

Placeholders follow the same scoping rules as in template bodies — resolved from the current iteration scope inside a `loop`, and from the root otherwise. Global params use the `global.` prefix:

```
"${global.env} == 'prod'"
```

A placeholder absent from the params map resolves to `null`. Use [`is_null`](#is_null) to test for its presence.

---

### Comparison operators

| Operator | Meaning | Null-safe |
|----------|---------|-----------|
| `==` | Equal | ✅ (`null == null` is `true`) |
| `!=` | Not equal | ✅ |
| `>` | Greater than | ❌ |
| `>=` | Greater than or equal | ❌ |
| `<` | Less than | ❌ |
| `<=` | Less than or equal | ❌ |

`==` and `!=` use null-safe equality. Numeric values are normalised to `BigDecimal` before comparison, so `Integer(5)` and `Long(5)` are considered equal.

Ordering operators (`>`, `>=`, `<`, `<=`) require **both sides to be the same type**: `String`, `ZonedDateTime`, `Number`, or `Boolean`. Mixing types (e.g. a string placeholder against an integer literal) is a serialization error.

---

### Boolean operators

| Operator | Meaning |
|----------|---------|
| `&&` | Logical AND |
| `\|\|` | Logical OR |

Both operands must produce a `boolean`.

**Important:** mixing `&&` and `\|\|` at the same level **without parentheses is a parse error**. The engine does not apply implicit precedence between them — add parentheses to make the precedence explicit:

```
// ✅ precedence is explicit
"(${type} == 'CA' || ${type} == 'ROOT') && ${path_len} >= 0"

// ❌ parse error — mixed && and || without brackets
"${a} == 'x' || ${b} == 'y' && ${c} == 'z'"
```

---

### Inversion

Prefix any sub-expression, placeholder, or function call with `!` to negate its boolean result:

```
"!${is_ca}"
"!is_null(${san})"
"!(${type} == 'CA' || ${type} == 'ROOT')"
```

Double negation (`!!`) is automatically collapsed by the parser.

---

### Grouping

Parentheses control evaluation order. They are **required** when `&&` and `||` are mixed at the same level. Functions also use parentheses for their argument list — the parser distinguishes function calls from grouping by the name token before the opening bracket.

---

### Built-in functions

#### date_time

Converts a string to a `ZonedDateTime` for use with ordering operators. Without this function, comparing a string placeholder against a date literal with `<` / `>` is a type error.

```
date_time(argument)
```

| Argument type | Behaviour |
|---------------|-----------|
| `String` | Parsed with format `yyyy-MM-dd'T'HH:mm:ss'Z'` |
| `ZonedDateTime` | Returned as-is |
| Other | Serialization error |

```
"date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"
"date_time(${not_after}) > date_time(${not_before})"
```

---

#### length

Returns the length or size of the argument.

```
length(argument)
```

| Argument type | Returns |
|---------------|---------|
| `String` | Number of characters |
| `Collection` | Number of elements |
| Array | Array length |
| `null` | `0` |
| Other | Serialization error |

```
"length(${san_entries}) > 0"
"length(${cn}) <= 64"
```

---

#### is_null

Returns `true` if the argument is `null`, `false` otherwise. Use this to make a condition depend on whether an optional param was provided.

```
is_null(argument)
```

```
"is_null(${san})"
"!is_null(${policy_oid})"
```

---

### Type rules

The **final result of the top-level expression must be a boolean**. Expressions that produce a string, number, or any other type throw a serialization error.

| Construct | Produces |
|-----------|---------|
| Comparison (`==`, `!=`, `>`, ...) | `Boolean` |
| Boolean operator (`&&`, `\|\|`) | `Boolean` |
| Inversion (`!`) | `Boolean` |
| `is_null(...)` | `Boolean` |
| `date_time(...)` | `ZonedDateTime` — must be used inside a comparison |
| `length(...)` | `Integer` — must be used inside a comparison |
| Bare placeholder | Type of the resolved value — must be `Boolean` when used as the full expression |
| `true` / `false` literal | `Boolean` |

---

### Expression examples

```
// field equality
"${type} == 'CA'"

// null guard — include extension only when value is present
"!is_null(${certificate_policy})"

// RFC 5280 date selection — UTCTime range
"date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"

// compound — CA or root with non-negative path length
"(${type} == 'CA' || ${type} == 'ROOT') && ${path_len} >= 0"

// length guard on a list
"length(${san_entries}) > 0"

// global param check
"${global.env} == 'prod'"

// boolean placeholder used directly as the full expression
"${include_san}"
```
