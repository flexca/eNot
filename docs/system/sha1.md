# System `sha1` element

← [Back to System elements](index.md)

---

## Description

The `sha1` element computes the SHA-1 digest of its input. Its body must resolve to exactly one binary value. The output is a single binary value containing the 20-byte SHA-1 hash.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"sha1"` |

---

## Example usage

### Subject Key Identifier (RFC 5280 §4.2.1.2)

The most common use case for `sha1` in X.509 templates is computing the Subject Key Identifier. Per RFC 5280 §4.2.1.2, the key identifier is the SHA-1 hash of the BIT STRING value of the SubjectPublicKeyInfo field.

In this example the caller supplies the raw DER-encoded public key as a hex string. `hex_to_bin` decodes it to binary, and `sha1` hashes it:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.14"
  - type: asn.1
    attributes:
      tag: octet_string
    body:
      type: asn.1
      attributes:
        tag: octet_string
      body:
        type: system
        attributes:
          kind: sha1
        body:
          type: system
          attributes:
            kind: hex_to_bin
          body: "${subject_public_key_hex}"
```

**params.yaml**
```yaml
subject_public_key_hex: "3059301306072a8648ce3d020106082a8648ce3d030107..."
```

The hex string is decoded to binary by `hex_to_bin`, the raw bytes are passed to `sha1`, and the resulting 20-byte digest is encoded as the OCTET STRING value.

**template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.14"
    },
    {
      "type": "asn.1",
      "attributes": { "tag": "octet_string" },
      "body": {
        "type": "asn.1",
        "attributes": { "tag": "octet_string" },
        "body": {
          "type": "system",
          "attributes": { "kind": "sha1" },
          "body": {
            "type": "system",
            "attributes": { "kind": "hex_to_bin" },
            "body": "${subject_public_key_hex}"
          }
        }
      }
    }
  ]
}
```

**params.json**
```json
{
  "subject_public_key_hex": "3059301306072a8648ce3d020106082a8648ce3d030107..."
}
```

---

### Authority Key Identifier (RFC 5280 §4.2.1.1)

`sha1` is equally used for the Authority Key Identifier extension, which stores the SHA-1 hash of the issuer's public key:

**template.yaml**
```yaml
type: asn.1
attributes:
  tag: octet_string
body:
  type: system
  attributes:
    kind: sha1
  body:
    type: system
    attributes:
      kind: hex_to_bin
    body: "${issuer_public_key_hex}"
```

---

## Optional behaviour

When `optional: true` is set on the `sha1` element and the body resolves to an empty list, the element emits no output and is silently skipped by the parent.

```yaml
type: system
optional: true
attributes:
  kind: sha1
body: "${optional_key_material}"
```

---

## Error conditions

| Condition | Behaviour |
|-----------|-----------|
| Body resolves to more than one value | Serialization fails — exactly one input is required. |
| Body value is not a binary-compatible type | Serialization fails with a type error. |
| Body is empty and `optional` is not `true` | Serialization fails because a required value is missing. |

---

## Notes

- Output is always exactly **20 bytes** (160 bits).
- SHA-1 is used here because it is mandated by RFC 5280 for key identifiers. For general-purpose integrity or security checks outside the X.509 profile, prefer a stronger hash algorithm.
- `sha1` is binary-in, binary-out. To obtain a hex-encoded digest, wrap the result in a [`bin_to_hex`](bin_to_hex.md) element.
