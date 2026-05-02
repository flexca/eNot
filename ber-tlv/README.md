# eNot BER-TLV Module

This module adds BER-TLV encoding support to the eNot template engine.

## What is it for?

BER-TLV (Basic Encoding Rules — Tag-Length-Value) is a binary encoding format widely used in smart card standards such as EMV, ISO 7816, and GlobalPlatform. Each data object consists of three fields:

- **Tag** — one to four bytes identifying the data object
- **Length** — encodes the number of value bytes (definite or indefinite form)
- **Value** — the raw byte content, which may itself contain nested TLV objects

This module lets you describe BER-TLV structures as eNot templates (JSON or YAML) and serialize them from parameter maps into binary output.

## Registration

Register `BerTlvEnotTypeSpecification` with `EnotRegistry` alongside any other type specifications you need:

```java
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecifications(
                new SystemTypeSpecification(),   // loop, condition, hex_to_bin, …
                new BerTlvEnotTypeSpecification()
        )
        .build();
```

### Bypassing tag validation for non-standard tags

By default the validator checks that every tag conforms to the ITU-T X.690 structural rules.
If your target specification uses proprietary tags that would otherwise fail this check,
pass their hex strings to the constructor:

```java
new BerTlvEnotTypeSpecification("1F", "9F81")
```

The listed tags will skip the X.690 structural validation while all other checks remain active.

## Attributes

| Attribute         | Type    | Required | Description                                                          |
|-------------------|---------|----------|----------------------------------------------------------------------|
| `tag`             | string  | yes      | Hex-encoded tag bytes, e.g. `"9F1A"` or `"04"`                      |
| `min_length`      | integer | no       | Minimum value length in bytes (inclusive). Must be non-negative.     |
| `max_length`      | integer | no       | Maximum value length in bytes (inclusive). Must be >= `min_length`.  |
| `indefinite_form` | boolean | no       | If `true`, encodes length as `0x80` with `00 00` terminator.        |

## Primitive (leaf) element — JSON

A primitive TLV element contains a raw binary value. Use the `system` `hex_to_bin` element to supply a hex-string parameter as binary:

```json
{
  "type": "ber-tlv",
  "attributes": {
    "tag": "5A"
  },
  "body": {
    "type": "system",
    "attributes": {
      "kind": "hex_to_bin"
    },
    "body": "${pan}"
  }
}
```

**Params:**
```json
{ "pan": "4111111111111111" }
```

**Output (hex):** `5A 08 41 11 11 11 11 11 11 11`

## Constructed (node) element — JSON

A constructed TLV element contains nested TLV children as its body list:

```json
{
  "type": "ber-tlv",
  "attributes": {
    "tag": "70"
  },
  "body": [
    {
      "type": "ber-tlv",
      "attributes": {
        "tag": "9F02"
      },
      "body": {
        "type": "system",
        "attributes": { "kind": "hex_to_bin" },
        "body": "${amount}"
      }
    },
    {
      "type": "ber-tlv",
      "attributes": {
        "tag": "9F1A"
      },
      "body": {
        "type": "system",
        "attributes": { "kind": "hex_to_bin" },
        "body": "${country_code}"
      }
    }
  ]
}
```

**Params:**
```json
{ "amount": "000000001000", "country_code": "0840" }
```

**Output (hex):** `70 0E 9F 02 06 00 00 00 00 10 00 9F 1A 02 08 40`

## Equivalent YAML template

```yaml
type: ber-tlv
attributes:
  tag: "70"
body:
  - type: ber-tlv
    attributes:
      tag: "9F02"
    body:
      type: system
      attributes:
        kind: hex_to_bin
      body: "${amount}"
  - type: ber-tlv
    attributes:
      tag: "9F1A"
    body:
      type: system
      attributes:
        kind: hex_to_bin
      body: "${country_code}"
```

## Length constraints

You can enforce value byte length bounds using `min_length` and `max_length`:

```json
{
  "type": "ber-tlv",
  "attributes": {
    "tag": "5F20",
    "min_length": 1,
    "max_length": 26
  },
  "body": {
    "type": "system",
    "attributes": { "kind": "hex_to_bin" },
    "body": "${cardholder_name}"
  }
}
```

If the serialized value length falls outside `[min_length, max_length]`, an `EnotSerializationException` is thrown. The check applies to the encoded value bytes only, not the full TLV structure.

## Indefinite form

Setting `indefinite_form: true` encodes the length field as `0x80` and appends a two-byte end-of-contents marker `00 00` after the value, following the BER indefinite-length encoding rule:

```json
{
  "type": "ber-tlv",
  "attributes": {
    "tag": "70",
    "indefinite_form": true
  },
  "body": [
    {
      "type": "ber-tlv",
      "attributes": { "tag": "04" },
      "body": {
        "type": "system",
        "attributes": { "kind": "hex_to_bin" },
        "body": "${data}"
      }
    }
  ]
}
```

**Output structure:** `70 80 <children> 00 00`

### Relaxation: indefinite form on primitive elements

Strictly speaking, BER (ISO/IEC 8825-1) permits indefinite-length encoding only for constructed encodings. This module intentionally relaxes that rule and allows `indefinite_form: true` on primitive (leaf) elements as well, producing `<tag> 80 <value> 00 00`. This is useful for certain proprietary or legacy smart card implementations that accept it. Do not enable this for strict BER/DER compliance.

## Limitation: maximum tag length is 4 bytes

The BER standard (ISO/IEC 8825-1) does not impose an upper limit on tag length — long-form tags can technically span any number of bytes. This module enforces a maximum of **4 bytes** (8 hex characters) per tag. Tags longer than 4 bytes are rejected during template validation with an `EnotParsingException`.

This covers all tag sizes used in practice by EMV, ISO 7816-4, and GlobalPlatform specifications, where the longest defined tags are 3 bytes.

## Relaxation: constructed bit not validated

BER uses bit 6 of the first tag byte to distinguish primitive from constructed encodings (0 = primitive, 1 = constructed). This module does **not** validate or enforce this bit. Whether an element is encoded as primitive or constructed is determined entirely by its eNot template body:

- A **body list** (array of child elements) → constructed encoding (node)
- A **binary body** (hex_to_bin or similar) → primitive encoding (leaf)

This allows maximum flexibility for non-standard and proprietary tag spaces, but means the library will happily encode a tag with the primitive bit set as a constructed element or vice versa if the template dictates it. Ensure your templates match the tag semantics of the target specification.
