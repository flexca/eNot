# Expression syntax

Expressions are used in the [`condition`](system.md#condition) system
element's `expression` attribute. An expression must evaluate to a
`boolean` value — any other result is a serialization error.

← [Back to Format overview](README.md) · [System elements](system.md#condition)

---

## Table of contents

- [Primitives](#primitives)
- [Placeholders in expressions](#placeholders-in-expressions)
- [Comparison operators](#comparison-operators)
- [Boolean operators](#boolean-operators)
- [Inversion](#inversion)
- [Grouping](#grouping)
- [Built-in functions](#built-in-functions)
  - [date_time](#date_time)
  - [length](#length)
  - [is_null](#is_null)
- [Type rules](#type-rules)
- [Examples](#examples)

---

## Primitives

| Syntax | Type | Example |
|--------|------|---------|
| `${name}` | Placeholder | `${cn}` |
| `'text'` | String literal (single quotes) | `'Alice'` |
| `42` | Integer literal | `42` |
| `true` / `false` | Boolean literal (case-insensitive) | `true` |
| `null` | Null literal | `null` |

---

## Placeholders in expressions

Placeholders follow the same resolution rules as in the template body —
they are resolved from the current scope (or global params with the
`global.` prefix):

```
"${cn} == 'Alice'"
"${global.env} == 'prod'"
```

A placeholder absent from the params map resolves to `null`. Use
[`is_null`](#is_null) to test for its presence:

```
"!is_null(${san})"
```

---

## Comparison operators

| Operator | Meaning | Null-safe |
|----------|---------|-----------|
| `==` | Equal | ✅ (`null == null` is `true`) |
| `!=` | Not equal | ✅ |
| `>` | Greater than | ❌ |
| `>=` | Greater than or equal | ❌ |
| `<` | Less than | ❌ |
| `<=` | Less than or equal | ❌ |

`==` and `!=` use null-safe equality. Numeric values are normalised to
`BigDecimal` before comparison, so `Integer(5)` and `BigInteger(5)` are
considered equal.

Ordering operators (`>`, `>=`, `<`, `<=`) require both sides to be the
**same type**: `String`, `ZonedDateTime`, `Number`, or `Boolean`. Mixing
types (e.g. a string placeholder against an integer literal) is a
serialization error.

---

## Boolean operators

| Operator | Meaning |
|----------|---------|
| `&&` | Logical AND — evaluates all parts |
| `\|\|` | Logical OR — evaluates all parts |

Both sides of a binary operator must produce a `boolean` value.

**Important:** mixing `&&` and `\|\|` at the same level **without
parentheses** is a parse error, because the engine does not apply
implicit precedence between them. Add brackets to make the precedence
explicit:

```
// ✅  precedence is clear
"(${type} == 'CA' || ${type} == 'ROOT') && ${path_len} >= 0"

// ❌  parse error — mixed && and || without brackets
"${a} == 'x' || ${b} == 'y' && ${c} == 'z'"
```

---

## Inversion

Prefix any sub-expression, placeholder, or function call with `!` to
negate its boolean result:

```
"!${is_ca}"
"!is_null(${san})"
"!(${type} == 'CA' || ${type} == 'ROOT')"
```

Double negation (`!!`) is automatically collapsed by the parser.

---

## Grouping

Parentheses group sub-expressions and control evaluation order.
Parentheses are **required** whenever `&&` and `||` are mixed at the
same level.

```
"(${a} == '1' || ${a} == '2') && ${b} != null"
```

Functions also use parentheses for their argument list — the parser
distinguishes function calls from grouping by the name prefix before the
opening bracket.

---

## Built-in functions

### date_time

Converts a string to a `ZonedDateTime` so it can be used with ordering
operators.

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

> **Note:** Comparing a plain string placeholder directly with a date
> string literal using `<` / `>` is a type error. Always wrap datetime
> values in `date_time(...)` for ordering comparisons.

---

### length

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

### is_null

Returns `true` if the argument is `null`, `false` otherwise. Useful for
guarding optional fields.

```
is_null(argument)
```

```
"is_null(${san})"
"!is_null(${policy_oid})"
```

---

## Type rules

The final result of an expression **must be a boolean**. Expressions
that produce a string, number, or any other type are a serialization
error.

Summary of what each construct produces:

| Construct | Produces |
|-----------|---------|
| Comparison (`==`, `!=`, `>`, ...) | `Boolean` |
| Binary operator (`&&`, `\|\|`) | `Boolean` |
| Inversion (`!`) | `Boolean` (errors if operand is not boolean) |
| `is_null(...)` | `Boolean` |
| `date_time(...)` | `ZonedDateTime` — must be used in a comparison |
| `length(...)` | `Integer` — must be used in a comparison |
| Bare placeholder | Type of the resolved value — must be `Boolean` for a top-level expression |
| `true` / `false` | `Boolean` |

---

## Examples

```
// field equality
"${type} == 'CA'"

// null guard
"!is_null(${san})"

// RFC 5280 date selection — UTCTime range
"date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"

// compound — CA or root with path length constraint
"(${type} == 'CA' || ${type} == 'ROOT') && ${path_len} >= 0"

// length guard on a list
"length(${san_entries}) > 0"

// global param check
"${global.env} == 'prod'"

// optional field — include extension only when value is present
"!is_null(${certificate_policy})"

// boolean placeholder used directly as top-level result
"${include_san}"
```
