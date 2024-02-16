# G2pc Core Lib

## JSON schema validations 
This project is an implementation of the JSON Schema Draft v4, 

### When to use Json schema
Let'+s assume that you already know what JSON Schema is, 
and you want to utilize it in a Java application to validate JSON data. But - as you may have already discovered - there is also an other Java implementation of the JSON Schema specification. So here are some advices about which one to use:

1. if you use Jackson to handle JSON in Java code, then java-json-tools/json-schema-validator is obviously a better choice, since it uses Jackson
2. if you want to use the org.json API then this library is the better choice
3. if you need JSON Schema Draft 6 / 7 support, then you need this library.

### Maven Dependency 
````
<dependency>
	<groupId>com.github.erosb</groupId>
	<artifactId>everit-json-schema</artifactId>
	<version>1.14.2</version>
</dependency>
````

### Where we used the json schema
* We have 2 end-points , /search and /on-search. 
* In these end-points we are receiving 2 payloads respectively.
* Each payload had header and message.
* We are using JSON schema to validate the header and message as per G2p specifications. 

#### Below are some samples schema which written in this project.
````
{
  "$schema": "https://json-schema.org/draft-04/schema#",
  "$id": "https://example.com/message.schema.json",
  "title": "header schema",
  "description": "",
  "additionalProperties": false,
  "type": "object",
  "properties": {
    "type": {
      "type": "string"
    },
    "version" : {
      "type": [ "string", "null" ]
    },
    },
    "total_count": {
      "type": "number"
    },
    "is_msg_encrypted": {
      "type": ["boolean","null"],
      "default": "false"
    },
    "meta": {
      "type": [ "object", "null" ]
    }
  },
  "required": ["message_id","message_ts","action","sender_id","total_count"],
  "definitions": {
    "nonEmptyString": {
      "type": "string",
      "minLength": 1
    }
  }
}
````

Using below code we can read the above schema json
````
InputStream schemaStream = CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/ResponseMessageschema.json");
````


