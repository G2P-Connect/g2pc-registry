# G2pc DC Core Lib

## Overview
### Json Schema validations
* In this project Json schema input stream return by parent g2p-core lib.
* In ResponseHandlerServiceImpl class input stream is called and will validate the Response DTO header and message
* If any thing doesn't match with the json schema exception handling is written for same.
* Below are some reference code for same.

````
   InputStream schemaStream = commonUtils.getRequestMessageString();
        JsonNode jsonNodeMessage = objectMapper.readTree(messageString);
        JsonSchema schemaMessage = null;
        if(schemaStream !=null){
            schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStream);
        }
        Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
        List<G2pcError> errorcombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorcombinedMessage.add(new G2pcError("",error.getMessage()));

        }
````