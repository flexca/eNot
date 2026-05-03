# eNot — Encoding Notations

[![Build](https://github.com/flexca/eNot/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/flexca/eNot/actions/workflows/continuous-integration.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.flexca/enot-core.svg)](https://central.sonatype.com/artifact/io.github.flexca/enot-core)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org)

**eNot** is a Java templating engine that transforms **JSON/YAML** structures into binary-encoded data (**ASN.1 DER**, **BER-TLV**, and more). By separating structure from values, eNot eliminates the fragility of hand-written encoding and provides a robust, maintainable way to generate complex binary payloads.

### Quick Start

#### 1. Add dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.flexca</groupId>
    <artifactId>enot-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or to `build.gradle`:

```groovy
implementation 'io.github.flexca:enot-core:1.0.0'
```

#### 2. Create a template
eNot templates mirror your data structure. For example, to encode this **ASN.1 definition**:

```asn1
ExampleStructure ::= SET {
    data  SEQUENCE {
        oid   OBJECT IDENTIFIER,
        name  UTF8String
    }
}
```

You define the following **template.yaml**:
```yaml
type: asn.1
attributes: { tag: set }
body:
  type: asn.1
  attributes: { tag: sequence }
  body:
    - type: asn.1
      attributes: { tag: object_identifier }
      body: "${my_oid}"
    - type: asn.1
      attributes: { tag: utf8_string }
      body: "${my_text}"
```
*Check out the [eNot reference](docs/enot.md) for a deep dive into elements and scoping.*


#### 3. Encode your data
Supply runtime values via `SerializationContext` to generate the binary output:

```java
import io.github.flexca.enot.core.exception.EnotException;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.Base64;
import java.util.List;

public class EnotQuickStart {

    // YAML template:
    private static final String ENOT_TEMPLATE = """
            type: asn.1
            attributes: { tag: set }
            body:
              type: asn.1
              attributes: { tag: sequence }
              body:
                - type: asn.1
                  attributes: { tag: object_identifier }
                  body: "${my_oid}"
                - type: asn.1
                  attributes: { tag: utf8_string }
                  body: "${my_text}"
            """;

    public static void main(String[] args) {

        // Jackson object mappers:
        ObjectMapper jsonObjectMapper = new ObjectMapper();                     // Required if you are using JSON
        ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());    // Required if you are using YAML

        // Registry allow to supply eNot type specification:
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecification(new SystemTypeSpecification())    // System elements - loops, conditions and other utilites
                .withTypeSpecification(new Asn1TypeSpecification())      // ASN.1 elements - require to serialize to ASN.1 DER
                .build();

        // Enot - facade for parsing, serialization, etc:
        Enot enot = new Enot.Builder()
                .withRegistry(registry)                    // Adding registry
                .withJsonObjectMapper(jsonObjectMapper)    // Add JSON ObjectMapper if you are using JSON templates
                .withYamlObjectMapper(yamlObjectMapper)    // Add YAML ObjectMapper if you are using YAML templates
                .build();

        // Serialization context - hold params to replace placeholders:
        SerializationContext serializationContext = new SerializationContext.Builder()
                .withParam("my_oid", "1.2.840.113549.1.1.1")    // Value to replace ${my_oid} placeholder in template
                .withParam("my_text", "eNot")                   // Value to replace ${my_text} placeholder in template
                .build();

        try {
            // Serializing template with params to binary:
            List<byte[]> result = enot.serialize(ENOT_TEMPLATE, serializationContext);
            // Encoding result to Base64:
            String resultBase64Encoded = Base64.getEncoder().encodeToString(result.get(0));
            // Printing Base64 encoded result:
            System.out.println("Result: " + resultBase64Encoded);
        } catch (EnotException e) {
            e.printStackTrace();
        }
    }
}
```

### Key Features

- **Human-readable templates** — eNot templates are plain JSON or YAML files that mirror the binary structure they produce. They are easy to read, review, and version-control. Because templates are data, not code, they can be updated and reloaded at runtime without rebuilding the application. See [eNot format reference](docs/enot.md).

- **ASN.1 DER encoding** — Full support for DER-encoded ASN.1 structures: sequences, sets, OIDs, strings, integers, time types, context tagging, and more. See [ASN.1 elements](docs/asn1/index.md).

- **BER-TLV support** — BER-TLV encoding is available as a plug-in module, demonstrating that the engine is not tied to ASN.1. See [ber-tlv module](ber-tlv/README.md).

- **Control logic** — [`loop`](docs/system/loop.md) and [`condition`](docs/system/condition.md) system elements make templates truly flexible: iterate over parameter lists, include elements conditionally, and handle variable-length structures without any code changes.

- **Composition** — Break large templates into smaller, named pieces and reuse them with the [`reference`](docs/system/reference.md) system element. Build complex structures from tested, independently maintained parts.

- **Extensibility** — The type system is open: register your own element types alongside the built-in ones. See [Adding a new eNot element type](docs/add-new-element-type.md).

- **Interactive web tool** — Try eNot instantly in the browser without writing any Java. The web tool ships as a Docker image with a side-by-side template/params editor, YAML/JSON switching, and one-click example generation. See [web-tool](web-tool/README.md).

- **Detailed documentation** — Every element type, attribute, and constraint is documented with examples. See [Documentation](docs/index.md).

> [!NOTE]
> **eNot is designed for small, structured payloads** — certificate fields, extensions, smart-card commands, and similar structures that are kilobytes in size. The engine holds the entire structure in memory during serialization. It is not suitable for large binary payloads (hundreds of megabytes), and using it for such workloads — especially under concurrent load — may cause out-of-memory errors.

---

### Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `core` | `enot-core` | Parser, serializer, expression engine, type registry, and all built-in element types (ASN.1 DER + system elements). The only dependency needed for most use cases. |
| `ber-tlv` | `enot-ber-tlv` | BER-TLV encoding support. A plug-in module that demonstrates the extensible type system beyond ASN.1. |
| `web-tool` | — | Browser-based interactive playground for evaluating eNot templates. Not a library dependency — run it via Docker or as a standalone JAR. |

---

### Contributing

Issues and pull requests are welcome. If you find a bug, have a feature request, or want to add support for a new encoding format, please open an issue on [GitHub](https://github.com/flexca/eNot/issues).

---

### Acknowledgements

eNot is built on top of several excellent open-source libraries:

- **[Bouncy Castle](https://www.bouncycastle.org/)** — ASN.1 DER encoding and cryptographic primitives. The core of what makes eNot's ASN.1 support possible.
- **[Jackson](https://github.com/FasterXML/jackson)** — JSON and YAML parsing for template loading.
- **[Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)** and **[Apache Commons Collections](https://commons.apache.org/proper/commons-collections/)** — utility support throughout the library.
- **[Lombok](https://projectlombok.org/)** — boilerplate reduction in the Java source.

---

### License
Apache License 2.0 — see [LICENSE](LICENSE).