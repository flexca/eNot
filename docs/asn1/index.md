# ASN.1 elements

← [Back to Documentation](../index.md)

---

Elements with `"type": "asn.1"` are encoded using ASN.1 DER rules via the BouncyCastle library. The specific encoding is controlled by the mandatory `tag` attribute.

```yaml
type: asn.1
attributes:
  tag: <tag-name>
  # additional tag-specific attributes
body: ...
```

```json
{
  "type": "asn.1",
  "attributes": { "tag": "<tag-name>" },
  "body": "..."
}
```

---

## Registering ASN.1 elements

All ASN.1 tags are bundled in `Asn1TypeSpecification`. Register it when building the `EnotRegistry`:

```java
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;

EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecification(new SystemTypeSpecification())
        .withTypeSpecification(new Asn1TypeSpecification())
        .build();
```

No additional configuration is required. Once registered, all tags listed below are available in templates.

---

## ASN.1 tags

### Structural

| Tag | Description | Page |
|-----|-------------|------|
| `sequence` | DER SEQUENCE — ordered collection of child elements. | [sequence.md](sequence.md) |
| `set` | DER SET — unordered collection, same syntax as `sequence`. | [set.md](set.md) |

### Primitive

| Tag | Description | Page |
|-----|-------------|------|
| `object_identifier` | Dotted-decimal OID. Supports `allowed_values` validation. | [object_identifier.md](object_identifier.md) |
| `boolean` | DER BOOLEAN — `true` (`0xFF`) or `false` (`0x00`). | [boolean.md](boolean.md) |
| `integer` | DER INTEGER — any precision (int, long, BigInteger). | [integer.md](integer.md) |
| `octet_string` | DER OCTET STRING — wraps binary output. Supports `min_length`/`max_length`. | [octet_string.md](octet_string.md) |
| `bit_string` | DER BIT STRING — wraps binary output. Supports `apply_padding` for named-bit-list types. | [bit_string.md](bit_string.md) |
| `null` | DER NULL — two-byte `05 00`, no body required. | [null.md](null.md) |

### String

| Tag | Character set | Supports constraints | Page |
|-----|--------------|---------------------|------|
| `utf8_string` | Full Unicode | `min_length`, `max_length`, `allowed_values` | [utf8_string.md](utf8_string.md) |
| `printable_string` | Letters, digits, `' ( ) + , - . / : = ?`, space | `min_length`, `max_length`, `allowed_values` | [printable_string.md](printable_string.md) |
| `ia5_string` | 7-bit ASCII (0–127) | `min_length`, `max_length`, `allowed_values` | [ia5_string.md](ia5_string.md) |
| `visible_string` | Printable ASCII (32–126) | `min_length`, `max_length`, `allowed_values` | [visible_string.md](visible_string.md) |
| `bmp_string` | UCS-2 / BMP (U+0000–U+FFFF) | `min_length`, `max_length`, `allowed_values` | [bmp_string.md](bmp_string.md) |

### Time

| Tag | Description | Page |
|-----|-------------|------|
| `generalized_time` | DER GeneralizedTime — required for dates ≥ 2050 (RFC 5280). | [generalized_time.md](generalized_time.md) |
| `utc_time` | DER UTCTime — required for dates before 2050 (RFC 5280). | [utc_time.md](utc_time.md) |

### Context tagging

| Tag | Description | Page |
|-----|-------------|------|
| `tagged_object` | Wraps body in a context-specific `[n]` tag. Requires either `implicit` or `explicit` context tag number. | [tagged_object.md](tagged_object.md) |
