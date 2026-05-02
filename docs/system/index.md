# System elements

← [Back to Documentation](../index.md)

---

Elements with `"type": "system"` provide control-flow, scoping, and binary transformation capabilities. The specific behaviour is selected by the mandatory `kind` attribute inside `attributes`.

```yaml
type: system
attributes:
  kind: <kind-name>
  # additional kind-specific attributes
body: ...
```

```json
{
  "type": "system",
  "attributes": { "kind": "<kind-name>" },
  "body": "..."
}
```

---

## Registering system elements

All system elements are bundled in `SystemTypeSpecification`. Register it once when building the `EnotRegistry`:

```java
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;

EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecification(new SystemTypeSpecification())
        .withTypeSpecification(new Asn1TypeSpecification())   // or other types you use
        .build();
```

No additional configuration is required. Once registered, all eight `kind` values listed below are available in templates.

---

## Elements

| Kind | Summary | Page |
|------|---------|------|
| `loop` | Iterates over a named array parameter, serializing the body once per item and concatenating the results. | [loop.md](loop.md) |
| `condition` | Serializes its body only when a boolean expression evaluates to `true`; produces no bytes otherwise. | [condition.md](condition.md) |
| `group` | Steps the params scope into a named sub-object before serializing the body — a pure scoping wrapper with no output change. | [group.md](group.md) |
| `reference` | Loads and inlines another eNot template at parse time via a registered `EnotElementReferenceResolver`. | [reference.md](reference.md) |
| `bit_map` | Packs an array of boolean values into binary bytes according to configurable `byte_order` and `bit_order`. | [bit_map.md](bit_map.md) |
| `sha1` | Computes the SHA-1 digest of its binary input and outputs the 20-byte digest as binary. | [sha1.md](sha1.md) |
| `hex_to_bin` | Decodes a hexadecimal text string into raw binary bytes. | [hex_to_bin.md](hex_to_bin.md) |
| `bin_to_hex` | Encodes a binary value as a lowercase hexadecimal text string. | [bin_to_hex.md](bin_to_hex.md) |
