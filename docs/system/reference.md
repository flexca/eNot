# System `reference` element

← [Back to System elements](index.md)

---

## Description

The `reference` element loads and inlines another eNot template at parse time. It lets you break large templates into smaller, reusable files and compose them together — similar to `#include` in C or imports in other template engines.

The element has no body in the template. Instead, the body is supplied by an `EnotElementReferenceResolver` implementation that you register with `EnotRegistry`. When the parser encounters a `reference` element it calls your resolver, which returns the parsed elements of the referenced template. Those elements are inlined in place of the `reference` element as if they had been written there directly.

| Attribute | Required | Type | Description |
|-----------|----------|------|-------------|
| `kind` | ✅ | text | Must be `"reference"` |
| `reference_type` | ✅ | text | Identifies which registered `EnotElementReferenceResolver` to invoke. Must contain only letters, digits, or underscores. |
| `reference_identifier` | ✅ | text | Passed to the resolver as the address of the target template. The meaning of this value is defined entirely by the resolver — it can be a file path, a URI, a database key, or anything else. Must not be blank. |

---

## Registering a resolver

Before using a `reference` element you must register at least one `EnotElementReferenceResolver` with `EnotRegistry`. The resolver's `getReferenceType()` method must return the same string you use as `reference_type` in your templates.

```java
public class FileReferenceResolver implements EnotElementReferenceResolver {

    private final Path templatesRoot;

    public FileReferenceResolver(Path templatesRoot) {
        this.templatesRoot = templatesRoot;
    }

    @Override
    public String getReferenceType() {
        return "file"; // matches reference_type: "file" in templates
    }

    @Override
    public List<EnotElement> resolve(String referenceIdentifier,
                                     EnotContext enotContext,
                                     ParsingContext parsingContext) {
        try {
            String content = Files.readString(templatesRoot.resolve(referenceIdentifier));
            return enotContext.getEnotParser().parse(content, enotContext, parsingContext);
        } catch (IOException e) {
            throw new EnotInvalidArgumentException(
                    "cannot resolve reference: " + referenceIdentifier, e);
        }
    }
}
```

Register it when building the `EnotRegistry`:

```java
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecification(new SystemTypeSpecification())
        .withTypeSpecification(new Asn1TypeSpecification())
        .withElementReferenceResolver(new FileReferenceResolver(Path.of("templates/")))
        .build();
```

Multiple resolvers with different `reference_type` values can be registered in the same registry.

---

## Example usage

### Basic example

Consider a Subject Alternative Name (SAN) extension template that is reused across multiple certificate templates. Instead of duplicating the SAN structure, define it once in its own file and reference it.

**san-dns.yaml** (the referenced template)
```yaml
type: system
optional: true
attributes:
  kind: loop
  items_name: dns_name
body:
  type: asn.1
  optional: true
  attributes:
    tag: ia5_string
  body: "${value}"
```

**san-dns.json** (equivalent JSON form)
```json
{
  "type": "system",
  "optional": true,
  "attributes": {
    "kind": "loop",
    "items_name": "dns_name"
  },
  "body": {
    "type": "asn.1",
    "optional": true,
    "attributes": { "tag": "ia5_string" },
    "body": "${value}"
  }
}
```

**main-template.yaml** (the template that includes it)
```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes: { tag: object_identifier }
    body: "2.5.29.17"
  - type: system
    attributes:
      kind: reference
      reference_type: file
      reference_identifier: san-dns.yaml
```

**main-template.json**
```json
{
  "type": "asn.1",
  "attributes": { "tag": "sequence" },
  "body": [
    {
      "type": "asn.1",
      "attributes": { "tag": "object_identifier" },
      "body": "2.5.29.17"
    },
    {
      "type": "system",
      "attributes": {
        "kind": "reference",
        "reference_type": "file",
        "reference_identifier": "san-dns.yaml"
      }
    }
  ]
}
```

**params.yaml**
```yaml
dns_name:
  - value: example.com
  - value: www.example.com
```

**params.json**
```json
{
  "dns_name": [
    { "value": "example.com" },
    { "value": "www.example.com" }
  ]
}
```

The `reference` element is resolved at parse time. The parser calls `FileReferenceResolver.resolve("san-dns.yaml", ...)`, which parses that file and returns its elements. During serialization the result is exactly the same as if the loop had been written inline — the `reference` element itself produces no wrapping structure.

---

### Param scope inside a reference

The referenced template shares the **same serialization context** as the parent template. All params available at the point where the `reference` element appears are accessible inside the referenced template, including loop-scope variables when the reference is nested inside a `loop` body.

This means if your main template has a `loop` over `dns_name` and the loop body contains a `reference`, the referenced template can use `${value}` from the current iteration just as if it were inline code.

---

## Cyclic dependency detection

The parser tracks every resolved reference by its composite identifier (`reference_type:reference_identifier`). If the same identifier is encountered a second time **within a single parse chain**, the parser throws an `EnotInvalidArgumentException` with the message `"cyclic dependency detected for element with composite identifier: <type>:<identifier>"`.

Two patterns that are detected:

**Self-reference** — a template that references itself:

```json
{
  "type": "system",
  "attributes": {
    "kind": "reference",
    "reference_type": "file",
    "reference_identifier": "self.json"
  }
}
```

→ `self.json` tries to resolve `file:self.json` which is already in the parse chain → throws immediately.

**Mutual reference** — A references B, B references A:

```
template-a.yaml → references template-b.yaml
template-b.yaml → references template-a.yaml
```

→ When parsing `template-b.yaml` the parser sees `file:template-a.yaml` is already in the chain → throws.

**Diamond pattern** (A → B, A → C, both B and C → D) is **not** a cycle and is allowed. D is resolved independently for each path, and neither B nor C sees the other in its parse chain.

---

## Multiple reference types

You can register multiple resolvers to load templates from different sources in the same registry:

```java
EnotRegistry registry = new EnotRegistry.Builder()
        .withTypeSpecification(new SystemTypeSpecification())
        .withTypeSpecification(new Asn1TypeSpecification())
        .withElementReferenceResolver(new FileReferenceResolver(Path.of("templates/")))
        .withElementReferenceResolver(new ClasspathReferenceResolver())
        .withElementReferenceResolver(new DatabaseReferenceResolver(dataSource))
        .build();
```

Then in templates you select the source with `reference_type`:

**template.yaml**
```yaml
body:
  - type: system
    attributes:
      kind: reference
      reference_type: file
      reference_identifier: local/rdn.yaml
  - type: system
    attributes:
      kind: reference
      reference_type: classpath
      reference_identifier: defaults/common-extensions.yaml
```

**template.json**
```json
{
  "body": [
    {
      "type": "system",
      "attributes": {
        "kind": "reference",
        "reference_type": "file",
        "reference_identifier": "local/rdn.yaml"
      }
    },
    {
      "type": "system",
      "attributes": {
        "kind": "reference",
        "reference_type": "classpath",
        "reference_identifier": "defaults/common-extensions.yaml"
      }
    }
  ]
}
```
