# ASN.1 `bit_string` tag

← [Back to ASN.1 elements](index.md)

---

## Description

Encodes a DER BIT STRING. The body must produce **binary** output — typically the output of a [`bit_map`](../system/bit_map.md) system element.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `tag` | ✅ | text | Must be `"bit_string"` |
| `apply_padding` | ❌ | boolean | Default `false`. When `true`, the engine trims trailing zero bytes from the binary input and computes the number of unused bits in the final byte. The DER encoding then includes the correct padding byte. Use this for ASN.1 named-bit-list types (e.g. Key Usage) where RFC 5280 requires minimal encoding. |

---

## Example usage

### Key Usage extension (RFC 5280 §4.2.1.3)

Key Usage is an ASN.1 named-bit-list type. DER minimal encoding requires trailing zero bytes to be removed and the number of unused bits in the final byte to be recorded. Set `apply_padding: true` and use a [`bit_map`](../system/bit_map.md) source:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: bit_string
  apply_padding: true
body:
  type: system
  attributes:
    kind: bit_map
    byte_order: big_endian
    bit_order: msb_first
  body:
    - "${digital_signature}"
    - "${non_repudiation}"
    - "${key_encipherment}"
    - "${data_encipherment}"
    - "${key_agreement}"
    - "${key_cert_sign}"
    - "${crl_sign}"
    - "${encipher_only}"
    - "${decipher_only}"
```

**params.yaml**
```yaml
digital_signature: true
non_repudiation: false
key_encipherment: true
data_encipherment: false
key_agreement: false
key_cert_sign: false
crl_sign: false
encipher_only: false
decipher_only: false
```

With `digital_signature` and `key_encipherment` set, `bit_map` produces `[0xA0, 0x00]`. With `apply_padding: true` the engine trims the trailing `0x00` and records 5 unused bits, producing the minimal DER BIT STRING `03 02 05 A0`.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": {
    "tag": "bit_string",
    "apply_padding": true
  },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "bit_map",
      "byte_order": "big_endian",
      "bit_order": "msb_first"
    },
    "body": [
      "${digital_signature}",
      "${non_repudiation}",
      "${key_encipherment}",
      "${data_encipherment}",
      "${key_agreement}",
      "${key_cert_sign}",
      "${crl_sign}",
      "${encipher_only}",
      "${decipher_only}"
    ]
  }
}
```

---

### Raw BIT STRING (no padding)

When the binary content is already in final form and no trimming is needed, omit `apply_padding`:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: bit_string
body:
  type: system
  attributes:
    kind: hex_to_bin
  body: "${raw_bits_hex}"
```

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "bit_string" },
  "body": {
    "type": "system",
    "attributes": { "kind": "hex_to_bin" },
    "body": "${raw_bits_hex}"
  }
}
```

---

## `apply_padding` behaviour

When `apply_padding: true`:

1. Trailing zero **bytes** are removed from the binary input.
2. The number of unused bits in the final remaining byte is calculated.
3. The padding count is prepended to the BIT STRING value in the DER encoding.

When `apply_padding: false` (default): the binary input is used as-is with zero unused bits declared.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body does not produce binary output | Serialization fails with a type error. |
| Body produces more than one value | Serialization fails — exactly one binary input is required. |
