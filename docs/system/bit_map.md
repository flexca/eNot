# System `bit_map` element

← [Back to System elements](index.md)

---

## Description

The `bit_map` element packs an array of boolean values into binary bytes. Each boolean in the body array becomes one bit. The output is a single binary value containing `⌈n/8⌉` bytes for `n` input booleans.

Two attributes control the packing order:

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"bit_map"` |
| `byte_order` | ✅ | text | `"big_endian"` — the first bits of the array fill the first byte of the output. `"little_endian"` — the first bits of the array fill the last byte of the output. |
| `bit_order` | ✅ | text | `"msb_first"` — within each byte, the first boolean of that byte's group occupies the most-significant bit (bit 7). `"lsb_first"` — within each byte, the first boolean occupies the least-significant bit (bit 0). |

---

## Packing mechanics

Given `n` input booleans, the engine produces `⌈n/8⌉` bytes. If `n` is not a multiple of 8, the final byte is padded with zero bits on the unused side (MSB side for `lsb_first`, LSB side for `msb_first`).

### Worked example — 4 booleans: `[true, false, true, true]`

| Configuration | Byte value | Explanation |
|---------------|-----------|-------------|
| `big_endian` + `msb_first` | `0b_1011_0000` = `0xB0` | bits land at positions 7, 6, 5, 4 of byte 0 |
| `big_endian` + `lsb_first` | `0b_0000_1101` = `0x0D` | bits land at positions 0, 1, 2, 3 of byte 0 |
| `little_endian` + `msb_first` | `0b_1011_0000` = `0xB0` | same byte value; with only one byte orientation is irrelevant |
| `little_endian` + `lsb_first` | `0b_0000_1101` = `0x0D` | same byte value; with only one byte orientation is irrelevant |

For more than 8 booleans the difference between `big_endian` and `little_endian` becomes visible:

- **`big_endian`**: boolean[0..7] → byte[0], boolean[8..15] → byte[1], …
- **`little_endian`**: boolean[0..7] → byte[last], boolean[8..15] → byte[last-1], …

---

## Example usage

### Key Usage extension (RFC 5280 §4.2.1.3)

RFC 5280 defines the Key Usage extension as a BIT STRING where named bit 0 is `digitalSignature`, named bit 1 is `nonRepudiation`, and so on. "Named bit N" in ASN.1 DER corresponds to bit position `(7 - N)` within the first byte of the BIT STRING value, which matches `big_endian` + `msb_first`.

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.15"
  - type: asn.1
    optional: true
    attributes:
      tag: boolean
    body: "${key_usage_critical}"
  - type: asn.1
    attributes:
      tag: octet_string
    body:
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

With `digital_signature=true` (bit 0, shift=7) and `key_encipherment=true` (bit 2, shift=5):

```
byte[0] = (1 << 7) | (1 << 5) = 0x80 | 0x20 = 0xA0
byte[1] = 0x00   (decipher_only, the 9th bit, is false)
```

The parent `bit_string` element with `apply_padding: true` trims trailing zero bytes and records the number of unused bits in the padding prefix, producing the compact DER BIT STRING `03 02 05 A0`.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.15"
    },
    {
      "type": "asn.1",
      "optional": true,
      "attributes": { "tag": "boolean" },
      "body": "${key_usage_critical}"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": {
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
    }
  ]
}
```

**params.json**
```json
{
  "digital_signature": true,
  "non_repudiation": false,
  "key_encipherment": true,
  "data_encipherment": false,
  "key_agreement": false,
  "key_cert_sign": false,
  "crl_sign": false,
  "encipher_only": false,
  "decipher_only": false
}
```

---

## Choosing `byte_order` and `bit_order`

| Protocol / standard | `byte_order` | `bit_order` | Rationale |
|---------------------|-------------|-------------|-----------|
| ASN.1 BIT STRING named bits (RFC 5280) | `big_endian` | `msb_first` | Named bit N occupies position `7-N` in the first byte |
| Most network protocols (big-endian bit fields) | `big_endian` | `msb_first` | Network byte order |
| Little-endian embedded protocols | `little_endian` | `lsb_first` | Least-significant byte and bit first |

When in doubt, consult the bit numbering convention of the target encoding standard.

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| `byte_order` attribute is absent | Validation fails before serialization begins. |
| `bit_order` attribute is absent | Validation fails before serialization begins. |
| `byte_order` value is not `"big_endian"` or `"little_endian"` | Validation fails with an unrecognised value error. |
| `bit_order` value is not `"msb_first"` or `"lsb_first"` | Validation fails with an unrecognised value error. |
| A body item resolves to a non-boolean value | Serialization fails with a type error. |
| Body array is empty | Produces zero bytes (empty binary output). |
