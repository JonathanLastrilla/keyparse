# keyparse
This project allows custom JSON key syntax

## Syntax
```
"key:[attr1:val1,attr2:val2]
```

## Usage
```
Basic Usage

Deserialize the JSON normally with Jackson:

ObjectMapper mapper = new ObjectMapper();

ObjectNode root =
        (ObjectNode) mapper.readTree(json);

Create a reader for the current object:

JacksonJsonKeyReader reader =
        JacksonJsonKeyReader.of(root);

Then request attributes for a logical key:

Optional<Map<String, String>> attributes =
        reader.attributes("product");
```
For:

```
{
  "product:[type:card,id:123]": "GD01-001"
}
```
the returned attributes are:

type = card
id   = 123

