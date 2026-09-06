# KeyParse

A schema-less, library-agnostic semantic layer for JSON property keys.

Repository: https://github.com/JonathanLastrilla/keyparse

## Overview

KeyParse extends ordinary JSON property names with optional metadata attributes.

For example:

```json
{
  "product:[type:card,id:123]": "GD01-001"
}
```

The physical JSON property is:

```text
product:[type:card,id:123]
```

KeyParse interprets it as:

```text
logical key: product
attributes:
  type = card
  id   = 123
```

The JSON value remains:

```text
GD01-001
```

KeyParse is designed to be **additive**. Existing JSON and Jackson behavior should remain unchanged unless KeyParse is explicitly enabled.

---

## Design Principles

### 1. Explicit opt-in

Applications enable Jackson integration explicitly:

```java
ObjectMapper mapper = new ObjectMapper();

mapper.registerModule(
    new KeyParseJacksonModule()
);
```

Without the module, Jackson sees the physical property name normally.

### 2. Normal JSON remains normal

A standard property:

```json
{
  "product": "GD01-001"
}
```

continues to behave exactly as ordinary Jackson JSON.

### 3. Physical and logical names are distinct

KeyParse does not replace the physical JSON property.

For:

```text
product:[type:card,id:123]
```

the physical name remains available while KeyParse can additionally expose:

```text
product
```

as the logical name.

### 4. Metadata is explicit

KeyParse metadata does not automatically become a bean field.

A POJO must explicitly request an attribute using `@PropertyAttribute`.

---

# Core Model

The core parser is library-agnostic.

The parsed representation is:

```java
public record ParsedKey(
        String key,
        Map<String, String> attributes
) {}
```

For:

```text
product:[type:card,id:123]
```

the result is conceptually:

```text
key = product

attributes:
    type = card
    id   = 123
```

Metadata values are intrinsically strings.

Type conversion happens only when metadata is explicitly mapped to a typed POJO field.

---

# Jackson Integration

The Jackson implementation currently consists of:

- `KeyParseJsonParser`
- `LogicalProperty`
- `KeyParseLogicalPropertyResolver`
- `KeyParseBeanDeserializer`
- `KeyParseJacksonModule`
- `PropertyAttribute`
- `JacksonJsonKeyReader`

## KeyParseJsonParser

`KeyParseJsonParser` wraps Jackson's parser and provides logical property names for compliant KeyParse keys.

For example:

```text
physical:
product:[type:card,id:123]

logical:
product
```

The physical property remains accessible through:

```java
getPhysicalCurrentName()
```

The parser also tracks parsed KeyParse properties and their metadata.

---

# Logical Property Resolution

The logical-property resolver establishes which physical JSON property represents a logical property.

## Exact properties have priority

Given:

```json
{
  "product": "A",
  "product:[type:card,id:123]": "B"
}
```

the logical property is:

```text
product = A
```

The exact property wins regardless of JSON property ordering.

## Attributed property without an exact property

Given:

```json
{
  "product:[id:123]": "B"
}
```

the logical property resolves to:

```text
product = B
```

## Multiple attributed properties

Given:

```json
{
  "product:[id:123]": "A",
  "product:[id:456]": "B"
}
```

the logical property is ambiguous.

The logical-property resolution layer rejects this condition rather than silently choosing one value.

## Unknown attributes

Unknown attributes are allowed.

For example:

```text
product:[type:card,edition:limited,foo:bar]
```

can still resolve to:

```text
product
```

The attributes remain metadata unless explicitly consumed.

## Malformed syntax

Malformed KeyParse syntax remains an ordinary JSON property.

KeyParse does not attempt to reinterpret invalid keys.

---

# POJO Mapping

With the Jackson module registered:

```java
ObjectMapper mapper = new ObjectMapper();

mapper.registerModule(
    new KeyParseJacksonModule()
);
```

a KeyParse property can map to an ordinary POJO property.

Example JSON:

```json
{
  "product:[type:card,id:123]": "GD01-001"
}
```

can map the logical value to:

```java
private String product;
```

while metadata can independently be mapped through annotations.

---

# PropertyAttribute

The current annotation is:

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyAttribute {

    String key();

    String attribute();

    boolean nullable() default true;
}
```

Example:

```java
@PropertyAttribute(
    key = "product",
    attribute = "type"
)
private String type;
```

This tells KeyParse:

> For logical property `product`, map the `type` metadata attribute to this field.

---

# Nullable Metadata

`nullable` controls whether an expected attribute may be absent.

The default is:

```java
nullable = true
```

Therefore:

```java
@PropertyAttribute(
    key = "product",
    attribute = "type"
)
private String type;
```

allows the metadata to be absent.

The field remains unchanged/null when the attribute is not available.

## Required metadata

A field can require an attribute:

```java
@PropertyAttribute(
    key = "product",
    attribute = "type",
    nullable = false
)
private String type;
```

If the required attribute is missing, deserialization fails with a `JsonMappingException`.

This also applies if the entire referenced KeyParse property is absent.

Example:

```json
{
  "product:[id:123]": "GD01-001"
}
```

with:

```java
@PropertyAttribute(
    key = "product",
    attribute = "type",
    nullable = false
)
private String type;
```

fails because `type` is required but absent.

---

# Metadata Type Conversion

KeyParse metadata itself is string-valued.

Conversion occurs when assigning an attribute to an explicitly typed POJO field.

Currently supported target types include:

```text
String
int / Integer
long / Long
boolean / Boolean
double / Double
float / Float
```

For example:

```java
@PropertyAttribute(
    key = "product",
    attribute = "id"
)
private Integer id;
```

can convert:

```text
id = "123"
```

to:

```text
Integer.valueOf("123")
```

Unsupported target types currently fail rather than being silently coerced.

---

# Unconsumed Metadata

KeyParse metadata that is not explicitly consumed by a POJO does not cause deserialization to fail.

Instead, the Jackson integration emits a Java Util Logging `WARNING`.

For example:

```json
{
  "product:[type:card]": "GD01-001"
}
```

when mapped to a POJO that has no:

```java
@PropertyAttribute(...)
```

for `product`, produces a warning.

The purpose is to make unexpected metadata visible without breaking consumers.

This follows the additive design:

```text
known metadata
    -> explicitly consumed

unknown metadata
    -> warning

ordinary JSON
    -> unchanged
```

---

# Top-Level Scope

KeyParse POJO metadata processing is intentionally **top-level only**.

There is currently no recursive KeyParse metadata scanner.

For example:

```json
{
  "product": {
    "name:[type:label]": "Gundam"
  }
}
```

does not cause the outer KeyParse-aware processing to recursively interpret:

```text
name:[type:label]
```

The nested object remains under normal Jackson handling.

This scope has been explicitly tested.

## Why?

The goal is to avoid silently changing the behavior of nested Jackson objects.

KeyParse processing should occur only where the application has explicitly entered the supported KeyParse-aware boundary.

---

# Compatibility Model

The intended behavior is:

```text
No KeyParse module
        |
        v
Normal Jackson behavior
```

and:

```text
KeyParse module registered
        |
        v
Top-level KeyParse-aware processing
        |
        +--> logical property resolution
        |
        +--> explicit metadata mapping
        |
        +--> metadata guardrails
        |
        +--> ordinary Jackson handling preserved elsewhere
```

KeyParse should not require existing applications to rewrite their normal bean mappings simply because producers begin adding metadata-bearing property names.

---

# Current Test Coverage

The implementation currently has coverage for:

## Core parsing

- KeyParse key parsing
- logical key extraction
- attribute extraction
- malformed/non-compliant keys

## Jackson read API

- reading compliant keys
- reading attributes
- nested object navigation
- avoiding unintended recursive discovery

## Logical resolution

- exact property priority
- attributed property resolution
- duplicate logical-property ambiguity
- coexistence of different logical properties
- unknown attributes

## POJO mapping

- logical property mapping
- independent metadata mapping
- typed metadata conversion
- required metadata
- optional metadata
- missing KeyParse property with required metadata
- unconsumed metadata warnings
- ordinary properties without warnings
- top-level-only processing

---

# Implementation Status

Current project progression:

| Ticket | Area | Status |
|---|---|---|
| KP-001 | Stabilize core key parser | Done |
| KP-002 | Define `ParsedKey` model | Done |
| KP-003 | Complete Jackson read-only API | Done |
| KP-004 | Prove Jackson compatibility | Done |
| KP-005 | Define logical-property rules | Done |
| KP-006 | Build logical-property resolver | Done |
| KP-007 | `KeyParseJacksonModule` | Implemented / validated |
| KP-008 | Basic POJO mapping | Done |
| KP-009 | Independent metadata mapping | Done |
| KP-010 | Nested POJO scope | Defined: top-level only |
| KP-011 | Collections / arrays | Next |
| KP-012 | Maps / dynamic objects | Planned |
| KP-013 | Jackson annotations | Planned |
| KP-014 | DB / ORM compatibility simulation | Planned |
| KP-015 | Full backwards-compatibility suite | Planned |
| KP-016 | Serialization | Planned |
| KP-017 | Documentation / migration | Planned |
| KP-018 | Release 0.1 | Planned |

---

# Next Area: Collections and Arrays

The next planned feature is collection/array handling.

The current design direction is to keep metadata associated with the **property**, not with individual collection elements.

For example:

```json
{
  "products:[type:cards]": [
    "GD01-001",
    "GD01-002"
  ]
}
```

should conceptually resolve as:

```text
logical property:
products

metadata:
type = cards

value:
[
    "GD01-001",
    "GD01-002"
]
```

Jackson should continue to handle the actual collection/array value normally.

No metadata semantics should be imposed on individual elements unless a future feature explicitly defines such behavior.

---

# Design Guardrails

The following constraints are currently intentional:

1. KeyParse is opt-in at the Jackson module level.
2. Exact properties take precedence over attributed properties.
3. Ambiguous logical properties are rejected.
4. Metadata is not automatically mapped to POJO fields.
5. `@PropertyAttribute` explicitly opts a field into metadata mapping.
6. Metadata values are intrinsically strings.
7. Explicit field types control conversion.
8. Required metadata (`nullable=false`) fails loudly.
9. Optional metadata (`nullable=true`) is ignored when absent.
10. Unconsumed top-level metadata generates a warning rather than breaking deserialization.
11. Ordinary JSON properties remain unaffected.
12. There is no recursive KeyParse scanner.
13. Nested objects remain under ordinary Jackson semantics.
14. Malformed KeyParse keys remain ordinary JSON properties.
15. Physical property names remain distinguishable from logical names.

---

# Repository

GitHub:

https://github.com/JonathanLastrilla/keyparse

The repository contains the current core parser and Jackson integration implementation.
