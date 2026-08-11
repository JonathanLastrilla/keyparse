# keyparse
This project allows custom JSON key syntax
- KeyParse does not recursively search nested objects.
- Non-compliant keys are ignored by the KeyParse layer.

## Syntax
```json
{
"key:[attr1:val1,attr2:val2]": "value"
}
```

## Usage
### Basic Usage
Deserialize the JSON normally with Jackson:
```java
ObjectMapper mapper = new ObjectMapper();
ObjectNode root =
        (ObjectNode) mapper.readTree(json);

//Create a reader for the current object:

JacksonJsonKeyReader reader =
        JacksonJsonKeyReader.of(root);

//Then request attributes for a logical key:

Optional<Map<String, String>> attributes =
        reader.attributes("product");
```

For:
```json
{
  "product:[type:card,id:123]": "GD01-001"
}
```

```java
attributes.ifPresent(attrs -> { 
        String type = attrs.get("type");
        String id = attrs.get("id");
        System.out.println(type);
        System.out.println(id); });
```
the returned attributes are:

type = card
id   = 123

