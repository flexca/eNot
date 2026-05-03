# eNot web-tool

## Description

eNot web-tool is a lightweight web service with a user interface that allows you to quickly test the serialization of eNot templates.

## How to Run

### Pull from docker hub and run

eNot web-tool is available in docker hub. Use next commands to pull and run docker image:

```shell
docker pull flexca/enot-web-tool:1.0.0
docker run -p 8080:8080 flexca/enot-web-tool:1.0.0
```

### Build locally

Build eNot project, run from eNot root directory:

```shell
mvn clean install
```

#### Option 1: Build and Run with Docker

```shell
cd web-tool
docker build -t enot-web-tool .
docker run -d -p 8080:8080 --name enot-web-tool enot-web-tool
```

#### Option 2: Run the JAR Directly

```shell
cd web-tool/backend/target
```

Replace `{version}` with the actual version, e.g., `1.0.0-SNAPSHOT`:

```shell
java -jar enot-web-tool-backend-{version}.jar
```

## How to Use

After starting the eNot web-tool, open your browser and go to:

```
http://localhost:8080
```

![eNot web-tool UI](docs-img/web-tool-ui.png)

The tool has two editors side by side — **Template** (left) and **Params** (right) — and a format selector in the header that switches both editors between YAML and JSON mode.

1. Select the format (`YAML` or `JSON`) that matches your template.
2. Enter your eNot template in the **Template** editor.
3. Enter serialization parameters in the **Params** editor in the same format, or press **Example Params** to auto-generate a params skeleton from your template.
4. Press **Serialize**.

The **Base64 Output** field displays the serialized output encoded in base64. If the template contains errors, an error panel appears below the editors describing what went wrong.

### Example

Template (YAML):

```yaml
type: asn.1
attributes:
  tag: sequence
body:
  - type: asn.1
    attributes:
      tag: object_identifier
    body: "2.5.29.37"
  - type: asn.1
    optional: true
    attributes:
      tag: boolean
    body: "${extended_key_usage_critical}"
  - type: asn.1
    attributes:
      tag: octet_string
    body:
      type: asn.1
      attributes:
        tag: sequence
      body:
        type: system
        attributes:
          kind: loop
          items_name: extended_key_usage
        body:
          type: asn.1
          attributes:
            tag: object_identifier
          body: "${usage}"
```

Params (YAML):

```yaml
extended_key_usage_critical: true
extended_key_usage:
  - usage: "1.3.6.1.5.5.7.3.1"
  - usage: "1.3.6.1.5.5.7.3.2"
```
