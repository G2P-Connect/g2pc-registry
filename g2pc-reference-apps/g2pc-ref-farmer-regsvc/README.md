# G2pc Ref Farmer Reg Svc

## JSON schema validations
This project is an implementation of the JSON Schema Draft v4,

### Custom validation using json schema 
For Farmer registry Query params are the 20% are custom changes , in which for this query param different schema has been written
in this repo.

### Maven Dependency
````
<dependency>
	<groupId>com.github.erosb</groupId>
	<artifactId>everit-json-schema</artifactId>
	<version>1.14.2</version>
</dependency>
````



#### Below are some samples schema which written in this project.
````
{
  "$schema": "https://json-schema.org/draft-04/schema#",
  "$id": "https://example.com/message.schema.json",
  "title": "Query schema",
  "description": "",
  "additionalProperties": false,
  "type": "object",
      "properties": {
        "query_name" : {
          "type": "string"
        },
        "query_params": {
          "type": "object",
          "properties": {
            "type": {
              "$ref": "#/definitions/nonEmptyString",
              "type": "string"
            },
            "farmer_id": {
              "type": "array",
              "items": {
                "$ref": "#/definitions/nonEmptyString",
                "type": "string"
              }
            },
            "season": {
              "$ref": "#/definitions/nonEmptyString",
              "type": "string"
            }
          },
          "required": ["farmer_id","season"]
        }
      },
  "required": ["query_params"],
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
  InputStream schemaStreamQuery = FarmerValidationServiceImpl.class.getClassLoader()
                .getResourceAsStream("schema/farmerQuerySchema.json");
        JsonNode jsonNode = objectMapper.readTree(queryString);
        JsonSchema schema = null;
        if(schemaStreamQuery !=null){
            schema  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStreamQuery);
        }
        Set<ValidationMessage> errorMessage = schema.validate(jsonNode);
        List<G2pcError> errorcombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorcombinedMessage.add(new G2pcError("",error.getMessage()));

        }
````


