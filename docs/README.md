# 1. Overview of G2p connect - Registry
- Governments give money to people for various reasons like subsidies, pensions, scholarships, and emergency help. People can choose how they want to receive this money, like in cash, through their bank, on their phone, or with vouchers.

- But, each government department has to set up its own system to check if people are eligible for the money, make sure transactions are real, and actually send the money. They have to talk to different departments to gather all the needed information, and this leads to a lot of duplicate work and problems.
- Simply putting all the data in one place doesn't work well because it can be a security risk and needs a lot of new systems. The usual approach of just putting things 'online' doesn't consider the real issues like politics, how people use technology, and the need for new ideas.
- A G2P DPI - Registry , is about creating a system that works together and respects privacy, security, and individual choices. The usual steps for giving money involve checking if people qualify, confirming their identity, and sending the money to their chosen method.
- To make this process better, G2P Connect suggests building a secure, decentralized system that different departments can customize. This way, they can share common elements, solve problems, and make the process more efficient.
- G2P Connect is an open-source project that helps different government agencies in a country work together to deliver digital payments from start to finish.
- The G2P transaction process involves an individual asking for money, providing their ID, and going through some security steps. The system checks if they qualify by looking at different government databases.

## 2. G2p specifications
- G2P Connect API Specifications is a project that makes it easy for different systems to work together. It sets rules for how they should talk to each other
- The main goals of G2P Connect Specifications are to make sure systems can work seamlessly together and follow the rules set by the country. It also aims to be flexible, meaning it can adapt to existing standards and use common methods like OAuth2 for security.
- The message structure used in G2P Connect is like a package with a signature and a header. The header includes important information like the version, message ID, and what action is being taken.
- The specifications also allow easy integration of different data types and ensure secure communication through digital signatures and encryption.
- The focus is on standardizing core interfaces, acting as connectors between solutions and enabling countries to implement a variety of use cases.
- G2P Connect doesn't care how systems send messages; it can work with different methods like HTTPS, messaging events, or file exchanges. It also makes sure that the dates, times, and currency codes used in the messages are in a format that everyone can understand.
- In simple terms, G2P Connect API Specifications is like a rulebook that different systems follow to work together when giving money to people. It's flexible, secure, and makes sure everyone understands each other.


# 3. Overview / List of libraries
### 3.1 G2pc-core-lib - 
- The G2pc-core-lib serves as a central hub for managing shared, reusable elements within the G2p specification
- This includes 80% of reusable features, DTOs, and configurations consistent across all data providers and consumers. 
- The core library encapsulates below essential components - 
    - Redis cache configurations
    - Unirest library settings 
    - Common constants 
    - G2p-specific DTOs and enums 
    - Common exception handling 
    - Security configurations 
    - Utility functions
- The DTOs outlined in this library correspond to the elements specified in the G2p specification’s endpoints. In essence, they represent the key components integral to the functionality described in the G2p standards. E.g -  HeaderDTO , MessageDTO , ResponseDTO , etc. further details are listed in the technical overview. 
- DTOs are constructed based on the OOPs principle of reusability. Attributes are shared between request bodies and responses are identified, and common elements are defined in parent DTOs. Any remaining specific attributes are then placed in corresponding child DTOs. These can be more explained by the below example.
E.g - HeaderDTO , RequestHeaderDTO and ResponseHeaderDTO , etc.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/HeaderDTORelationship.png)
- This module declares functionalities for token-based authentication, digital signature, and securing messages through encryption using various algorithms. 
- As per the requirement , these encryption and digital signature functions can be called in the below combination.  
  ![Alt text]( https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/Sign-encry-table.png "a title")
- Custom validation exception -
   - JSON schemas have been used for validating both request and response components. 
   - Below Custom validation exceptions are defined 
     - G2pHttpException 
     - G2pcValidationException
   - These exceptions are used to handle http requests errors which can be thrown from unirest endpoints. 
  
- Note - All these changes have been done on the basis of G2p specification. 


### 3.2 G2pc-dp-core-lib
- The G2pc-dp-core-lib defines entities and services specific to data providers. 
- It incorporates methods for constructing responses and managing requests within custom data provider services. 
- This library encapsulates both an entity and repositories responsible for handling the data stored in tables dedicated to message tracking and transaction information. 
- Also , service implementation manages the tracking of transactions, including saving request details, determining record counts, constructing search responses, and updating transaction statuses. 
- It also includes building cache requests, validating request headers and messages against JSON schemas, and handling transaction tracking in both Redis and a database. 
- It integrates with other services and provides detailed error handling and logging. Also constructing response DTOs, managing encryption and signatures, sending responses via HTTP, and handling tokens.

### 3.3 G2pc-dc-core-lib
- The G2pc-dc-core-lib defines entities and services specific to data consumers. 
- It incorporates methods for constructing requests and managing responses within custom data consumer services.
- This library defines functionality for building and sending requests in a secure manner, involving encryption, digital signatures, token management, and caching.
- Also provides functionality for updating a cache, validating response headers, and validating response messages against predefined JSON schemas.
- This service is basically to handle and process responses within a system.
- Below is the communication diagram of DP and DC -

  ![Alt text]( https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/DC-DP-communication.png "a title")

# 4.  Data Provider (DP) Implementation
- In DP implementation , as explained in Overview of libraries , dependency of G2pc-dp-core-lib needs to be added. 
- Data provider is going to act as provider as well as consumer as.
- As shown in Figure 2 data provider needs to implement the end point and also make calls to the endpoint of the data consumer. 
- Implementation explained in below point when it act like data provider -
  1. At first Data provider service needs to write the /search end-point. 
  2. This endpoint will receive a requestString transferred by the data consumer. 
  3. In this endpoint authentication also needs to be defined to ensure that the correct user is accessing the endpoint or not. 
  4. Also need to make sure that the correct signature and valid message is received. 
  5. This requestString will get validated as per g2p specification. Please refer to the link mentioned and image below.
 ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/search-endpoint-spec.png "a title")
  6. Once requestString gets validated data provider should save that data in redis cache and transaction data in db and send acknowledgement back to data consumer. Refer below sequence diagram for reference - 
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-search-sequence-diagram.png)
- Implementation explained in below point when it act like data consumer -
  1. When it acts like a consumer , it needs to define a scheduler. Scheduler is nothing but a framework that allows you to schedule and execute tasks at specific intervals or times. 
  2. In this scheduler , dp will check whether there is any data stored in pending status with a particular cache key corresponding to that data provider. 
  3. If it gets data it will build the response data and the call /on-search endpoint is defined in the data consumer .  
  4. Refer below for understanding of flow from dp to parent libraries. 
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-scheduler-seq-diagram.png)

# 5. How to create a Data Provider ?
1. Create a spring boot application with the latest spring-boot version , maven and Java 17. And Click on generate to download.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/spring_boot_dp_creation.png)
2. Extract the downloaded jar and open it in IDE. 
3. Add below dependencies in pom.xml
````
         <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>g2pc.dp.core.lib</groupId>
            <artifactId>g2pc-dp-core-library</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
            <version>2.15.0</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.5.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
            <version>1.6.15</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-web</artifactId>
            <version>6.1.2</version>
        </dependency>
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>4.4.0</version>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
            <version>6.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-jasper</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>
````
4. Create package structure shown below.                                                                                         
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dp-package-strcuture.png)
5. Add .p12 file for search and on-search.   
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/.p12-dp.png)
6. In the config package , create the ObjectMapperConfig.java class.
````
@Configuration
public class ObjectMapperConfig {
   @Bean
   public ObjectMapper objectMapper() {
       ObjectMapper objectMapper = new ObjectMapper();
       objectMapper.registerSubtypes(RequestHeaderDTO.class,
               ResponseHeaderDTO.class, HeaderDTO.class);
       return objectMapper;
   }
}

````
7. In the constants package , create Constants.java class.
````
package g2pc.ref.farmer.regsvc.constants;
public class Constants {
   private Constants() {
   }
   public static final String SEARCH_REQUEST_RECEIVED = "Search request received successfully";
   public static final String INVALID_RESPONSE = "Invalid Response received from server";
   public static final String CONFLICT = "CONFLICT";
   public static final String INVALID_AUTHORIZATION = "Invalid Authorization";
   public static final String CACHE_KEY_SEARCH_STRING = "request-farmer*";
   public static final String CACHE_KEY_STRING = "request-farmer-";
   public static final String CONFIGURATION_MISMATCH_ERROR = "Configurations are not matching ";
   public static final String STATUS_CACHE_KEY_STRING = "status-request-farmer-";
   public static final String STATUS_CACHE_KEY_SEARCH_STRING = "status-request-farmer*";
}

````
8. In the controller.rest package , create a RegistryController.java class.
````
@RestController
@Slf4j
@RequestMapping(produces = "application/json")
@Tag(name = "Provider", description = "Provider APIs")
public class RegistryController {
}

````
9. In the service package , create the respective DP ResponseBuilderService.java interface. Refer below example.
````
public interface FarmerResponseBuilderService {
   RegRecordFarmerDTO getRegRecordFarmerDTO(FarmerInfoEntity farmerInfoEntity);
   }
````
10. In the service package , create the respective ValidationService.java interface. Refer below example.
````
public interface FarmerValidationService {
   void validateRequestDTO (RequestDTO requestDTO) throws Exception;
    RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO ) throws Exception;
}

````
11. In the serviceImpl package create implemented classes of above interfaces for respective dps.
````
@Service
@Slf4j
public class FarmerResponseBuilderServiceImpl implements FarmerResponseBuilderService { 

}
````
````
@Service
@Slf4j
public class FarmerValidationServiceImpl implements FarmerValidationService {
 } 
````
17. Add below autowired dependencies in the RegistryController class.
````
    @Autowired
    private RequestHandlerService requestHandlerService;
    
    @Autowired
    FarmerValidationService farmerValidationService;
    
    @Autowired
    private DpCommonUtils dpCommonUtils;
    
    @Autowired
    private MsgTrackerRepository msgTrackerRepository;
    
     @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DpSftpPushUpdateService dpSftpPushUpdateService;

````
18. Add below application.yml and update as per below instructions.
````
spring:
  mvc:
    view:
      prefix: /WEB-INF/jsp/
      suffix: .jsp

    pathmatch:
      matching-strategy: ANT_PATH_MATCHER

  datasource:
    driverClassName: org.postgresql.Driver
    # Add db name, schema name, username and password for db connection as per your postgres/mysql connection.
    url: jdbc:postgresql://{Domain-id}}:{port}/{Database name}?currentSchema={Schema name}
    username: {username of database connection}
    password: {password of database connection}

    hikari:
      data-source-properties:
        stringtype: unspecified
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        maintainTimeStats: false
        maximum-pool-size: 5
        connection-timeout: 5000
  jpa:
    properties:
      hibernate:
        jdbc:
          lob:
            non_contextual_creation: true
      dialect: org.hibernate.dialect.PostgreSQLDialect
    hibernate.ddl-auto: none
    show-sql: false
    open-in-view: false
    generate-ddl: false
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration

  devtools:
    restart:
      additional-paths: src/main/webapp
      exclude: static/**,public/**

server:
  port: {add port as per requirement}
  error:
    include-message: always

# This is redis connection configuration
spring.data.redis:
  repositories.enabled: false
  host: {host domain of redis connection}
  password: {password of redis port}
  port: {port of redis connection}

# These are dc side url. Add respective host name e.g. - localhost and port e.g. - 8080
client:
  api_urls:
    client_search_api: http://{host}:{port}/private/api/v1/registry/on-search
    client_status_api: http://{host}:{port}/private/api/v1/registry/on-status

# Below are Keycloak configuration.
keycloak:
  # These configurations are given by dc that is the reason name is from_dc. 
  from_dc:
    # url is for creating token , add domain name e.g. - http://127.0.0.1:8081/ , it means 8081 is port on which keycloak is running. 
    url: "https://{domain of dc instance}/auth/realms/data-consumer/protocol/openid-connect/token"
    clientId: {client name given to client created in dc instance}
    clientSecret: {client secret of client from dc keycloak instance. Refer 8.7 for same}
  dp:
    # These are dp keycloak instance url , admin name and password which is given while creating instance.
    url: https://{domain of dp instance}/auth
    username: {username of dp instance}
    password: {password of dp instance}
    master:
      # master token url for particular dp keycloak, add domain name http://127.0.0.1:8081/ 
      url: https://{domain}/auth/realms/master/protocol/openid-connect/token
      getClientUrl: https://{domain}/auth/admin/realms/{client id of dp}/clients
      clientId: {client name given to admin client created in dp instance}
      clientSecret: {client secret of admin client from dp keycloak instance. Refer 8.7 for same}
    client:
      # dp client token url. Add domain name and realm-id
      url: https://{domain}/auth/realms/{realm-id}/protocol/openid-connect/token
      realm: {realm id}
      clientId: {dp client id}
      clientSecret: {client secret of client from dp keycloak instance. Refer 8.7 for same}

# Below configuration is for cryptography setting.
crypto:
  # to_dc means these configurations used for on-search communication.
  to_dc:
    # flag of encryption (use only small case)    
    support_encryption: true
    # flag of signature (use only small case)
    support_signature: true
    password: {password of on-search .p12 file}
    key_path: {keypath of on-search .p12 file}
    id: {dp id which will be common between dc and dp}
  from_dc:
    # flag of encryption (use only small case)    
    support_encryption: true
    # flag of signature (use only small case)
    support_signature: true
    password: {password of search .p12 file which will be given from dc}
    key_path: {keypath of search .p12 file which will be given from dc}

dashboard:
  dp_dashboard_url: "http://{domain}:{port}/d-solo/e62ae08b-a6e1-4095-af79-c36f02b8fae2/dp1-dashboard?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"

# sftp connection configurations. 
sftp:
  listener:
    # hostname of sftp connection of dp e.g. localhost 
    host: {host name}
    port: {port of sftp connection}
    user: {username of sftp connection}
    password: {password of sftp connection}
    remote:
      # path of sftp client for inbound e.g. /inbound 
      inbound_directory: {path mentioned in sftp client for inbound}
      outbound_directory: {path mentioned in sftp client for outbound}
    local:
      inbound_directory: {path created in local machine for inbound}
      outbound_directory: {path created in local machine for inbound}

  dc:
    # hostname of sftp connection of dc e.g. localhost 
    host: {host name}
    port: {port of sftp connection}
    user: {username of sftp connection}
    password: {password of sftp connection}
    remote:
      outbound_directory: {path mentioned in sftp client for outbound}

sunbird:
  enabled: true
  elasticsearch:
    host: {domain of elasticsearch like if running on local env i.e localhost} 
    port: {port mention in docker-compose for elastic search}
    scheme: http
```` 
20. Define below endpoint in RegistryController.
````
@Operation(summary = "Receive search request")
@ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
       @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
       @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
       @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
@PostMapping("/private/api/v1/registry/search")
public AcknowledgementDTO handleRequest(@RequestBody String requestString) throws Exception {

````
21. Add below code in the same method to add subtype in objectMapper to convert String in requestDTO in handleRequest().
````
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerSubtypes(RequestHeaderDTO.class,
       ResponseHeaderDTO.class, HeaderDTO.class);


RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
       readValue(requestString);
RequestMessageDTO messageDTO = null;
````
22. Add below code snippet handleRequest() to validate signature and encryption.
````
Map <String , Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
requestDTO.setMessage(messageDTO);
````
23. Add below code snippet in handleRequest() to validate requestDTO as per g2p specifications and  build cache request for Request string. In this buildCacheRequest it has already been defined in parent libraries , just need to call.
````
String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
try {
   farmerValidationService.validateRequestDTO(requestDTO);
            return requestHandlerService.buildCacheRequest(
                    objectMapper.writeValueAsString(requestDTO), cacheKey, CoreConstants.SEND_PROTOCOL_HTTPS);
} catch (G2pcValidationException e) {
   throw new G2pcValidationException(e.getG2PcErrorList());
}
catch (JsonProcessingException e){
   throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR , e.getMessage());
}
catch (Exception e){
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST , e.getMessage());
}

````
24. Add below 2 methods Custom Exception handling using spring boot annotations in RegistryController.
````
@ExceptionHandler(value
       = G2pcValidationException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ValidationErrorResponse
handleValidationException(
       G2pcValidationException ex) {
   return new ValidationErrorResponse(
           ex.getG2PcErrorList());
}

@ExceptionHandler(value
            = G2pHttpException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ErrorResponse handleG2pHttpStatusException(
         G2pHttpException ex) {
   return new ErrorResponse(ex.getG2PcError());
   }

````
25. Create below endpoint for clearing the db.
````
  @GetMapping("/public/api/v1/registry/clear-db")
    public void clearDb() throws G2pHttpException, IOException {
         dpCommonUtils.handleToken();
        msgTrackerRepository.deleteAll();
        log.info("DP-1 DB cleared");
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("DP-1 Redis cache cleared");
    }
````
26. Create below class DcSftpListener for handing sftp request.
````
package g2pc.ref.farmer.regsvc.controller.sftp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.dto.SftpDpData;
import g2pc.ref.farmer.regsvc.service.DpSftpPushUpdateService;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Configuration
@Slf4j
public class DcSftpListener {

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpLocalDirectoryInbound;

    @Value("${sftp.listener.local.outbound_directory}")
    private String sftpLocalDirectoryOutbound;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    FarmerValidationService farmerValidationService;

    @Autowired
    private DpSftpPushUpdateService dpSftpPushUpdateService;

    @SuppressWarnings("unchecked")
    @ServiceActivator(inputChannel = "sftpInbound")
    public void handleMessageInbound(Message<File> message) {
        try {
            File file = message.getPayload();
            log.info("Received Message from inbound directory of dp-1: {}", file.getName());
            if (ObjectUtils.isNotEmpty(file) && file.getName().contains(".json")) {
                String requestString = new String(Files.readAllBytes(file.toPath()));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerSubtypes(RequestHeaderDTO.class,
                        ResponseHeaderDTO.class, HeaderDTO.class);

                RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                        readValue(requestString);
                RequestMessageDTO messageDTO;

                Map<String, Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();

                messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
                requestDTO.setMessage(messageDTO);
                String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
                try {
                    farmerValidationService.validateRequestDTO(requestDTO);
                    requestHandlerService.buildCacheRequest(
                            objectMapper.writeValueAsString(requestDTO), cacheKey, CoreConstants.SEND_PROTOCOL_SFTP);
                } catch (G2pcValidationException e) {
                    throw new G2pcValidationException(e.getG2PcErrorList());
                } catch (JsonProcessingException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
            Files.deleteIfExists(Path.of(sftpLocalDirectoryInbound + "/" + file.getName()));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(Message<?> message) {
        Throwable error = (Throwable) message.getPayload();
        log.error("Handling ERROR: {}", error.getMessage());
    }
}
````
27. Create Query and Query param dto for data provider requirement in dto.request package. Below are examples.
````
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryFarmerDTO {


   @JsonProperty("query_params")
   private QueryParamsFarmerDTO queryParams;
}
````
````
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryParamsFarmerDTO {

   @JsonProperty("farmer_id")
   private String farmerId;

   @JsonProperty("season")
   private String season;
}

````
27. Create regRecordDTO for data provider as per on search endpoint requirement in dto.response package shown below.
````
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DataFarmerDTO  {

   @JsonProperty("reg_records")
   private List<RegRecordFarmerDTO> regRecords;
}
````
````
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegRecordFarmerDTO {

   @JsonProperty("farmer_id")
   private String farmerId;

   @JsonProperty("farmer_name")
   private String farmerName;

   @JsonProperty("season")
   private String season;

   @JsonProperty("payment_status")
   private String paymentStatus;

   @JsonProperty("payment_date")
   private String paymentDate;

   @JsonProperty("payment_amount")
   private Double paymentAmount;
}

````
28. To get data provider information create a data-provider info table in db , entity and repository as shown below.
````
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "farmer_info")
public class FarmerInfoEntity {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id", nullable = false)
   private Long id;

   private String farmerId;

   private String farmerName;

   private String season;

   private String paymentStatus;

   private String paymentDate;

   private Double paymentAmount;

````
Write your respective method using data jpa concept.
````
@Repository
public interface FarmerInfoRepository extends JpaRepository<FarmerInfoEntity, Long> {
   Optional<FarmerInfoEntity> findBySeasonAndFarmerId(String season, String farmerId);
}
````
29. Define below method in ValidationServiceImpl as it has implemented from ValidationService interface.
````
@Override
public RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO) throws Exception {
````
30. Define below autowired beans and configurations in ValidationServiceImpl.
````
    @Autowired
    RequestHandlerService requestHandlerService;

    @Value("${crypto.from_dc.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.from_dc.support_signature}")
    private boolean isSign;

    @Value("${crypto.from_dc.password}")
    private String p12Password;

    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${crypto.from_dc.key.path}")
    private String farmer_key_path;
````
31. Add below code snippet in signatureValidation method. This is the validation for signature and encryption to check whether this is transferred correctly.
    1. Check isSign flag , if yes check metadata given from controller is true or false , if not true throw error that configurations are not valid.
    2. Take farmerKeyPath for dp .p12 file.
    3. Check isEncrypt flag , if true check isMsgEncrypt flag from header is true or false , if not true throw error that configurations are not valid.
    4. Recreate signature using header and message. Verify both signature if error from verifySignature throw error signature invalid.
    5. Decrypt message , if successfully decrypted add in requestDto.
    6. Above logic is same in 4 cases of signature and encryption mentioned "Overview / List of libraries". 
````
       ObjectMapper objectMapper = new ObjectMapper();
        RequestMessageDTO messageDTO;
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            Resource resource = resourceLoader.getResource(farmer_key_path);
            InputStream fis = resource.getInputStream();
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = requestDTO.getMessage().toString();
                String data = requestHeaderString+messageString;
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid ->"+e.getMessage()));
                }
                catch(IOException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        log.info("Rejecting the on-search request in farmer as signature is not valid");
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(RequestMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = requestHeaderString+messageString;
                log.info("Signature ->"+requestSignature);
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }
                catch(IOException e){
                log.info("Rejecting the on-search request in farmer as signature is not valid");
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
            }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = requestDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(RequestMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
            }
        }
        requestDTO.setMessage(messageDTO);
        return messageDTO;
    
````
Implement below method from ValidationService in ValidationServiceImpl.
````
@Override
public void validateRequestDTO(RequestDTO requestDTO) throws G2pcValidationException, IOException {
````
Change QueryFarmerDTO , QueryParamsFarmerDTO , with respective data provider DTOs. In this method , validations of message and header methods are called from parent dp-core.
````
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerSubtypes(QueryDTO.class,
       QueryFarmerDTO.class, QueryParamsFarmerDTO.class);
byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
RequestMessageDTO messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
String headerString = new ObjectMapper()
       .writerWithDefaultPrettyPrinter()
       .writeValueAsString(requestDTO.getHeader());
RequestHeaderDTO headerDTO = objectMapper.readerFor(RequestHeaderDTO.class).
       readValue(headerString);
requestHandlerService.validateRequestHeader(headerDTO);
requestHandlerService.validateRequestMessage(messageDTO);
````
32. In Scheduler class define below autowired bean. these beans are from dp-core library and also custom created in dp.
````
    @Autowired
    private ResponseBuilderService responseBuilderService;

    @Autowired
    private FarmerResponseBuilderService farmerResponseBuilderService;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;
````
Define below method in Scheduler.
````
@Scheduled(cron = "0 */1 * ? * *")// runs every 1 min.
@Transactional
public void responseScheduler() throws IOException {
````  
33. Define try catch in the same method.
34. Add below snippet in method responseScheduler().
````
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
````
35.  Call method from txnTrackerRedisService to get cache list.
````
List<String> cacheKeysList = txnTrackerRedisService.getCacheKeys(Constants.CACHE_KEY_SEARCH_STRING);
````
36. Check whether in list the status in PNDG or not.
````
if (cacheDTO.getStatus().equals(HeaderStatusENUM.PDNG.toValue())) {
````
37. Add below code snippet to in FarmerResponseBuilderServiceImpl.
````
@Autowired
private FarmerInfoRepository farmerInfoRepository;


/**
* Get farmer records information from DB
*
* @param farmerInfoEntity required
* @return Farmer records
*/
@Override
public RegRecordFarmerDTO getRegRecordFarmerDTO(FarmerInfoEntity farmerInfoEntity) {
   RegRecordFarmerDTO dto = new RegRecordFarmerDTO();
   dto.setFarmerId(farmerInfoEntity.getFarmerId());
   dto.setFarmerName(farmerInfoEntity.getFarmerName());
   dto.setSeason(farmerInfoEntity.getSeason());
   dto.setPaymentStatus(farmerInfoEntity.getPaymentStatus());
   dto.setPaymentDate(farmerInfoEntity.getPaymentDate());
   dto.setPaymentAmount(farmerInfoEntity.getPaymentAmount());
   return dto;
}

/**
* Get farmer records information string
*
* @param queryDTOList required
* @return List of farmer records
*/
@Override
public List<String> getRegFarmerRecords(List<QueryDTO> queryDTOList) throws IOException {
   ObjectMapper objectMapper = new ObjectMapper();
   List<String> regFarmerRecordsList = new ArrayList<>();
   for (QueryDTO queryDTO : queryDTOList) {
       String queryParams = objectMapper.writeValueAsString(queryDTO.getQueryParams());
       QueryParamsFarmerDTO queryParamsFarmerDTO = objectMapper.readValue(queryParams, QueryParamsFarmerDTO.class);
       String farmerId = queryParamsFarmerDTO.getFarmerId();
       String season = queryParamsFarmerDTO.getSeason();
       Optional<FarmerInfoEntity> optional = farmerInfoRepository.findBySeasonAndFarmerId(season, farmerId);
       if (optional.isPresent()) {
           RegRecordFarmerDTO regRecordFarmerDTO = getRegRecordFarmerDTO(optional.get());
           regFarmerRecordsList.add(objectMapper.writeValueAsString(regRecordFarmerDTO));
       } else {
           regFarmerRecordsList.add(StringUtils.EMPTY);
       }
   }
   return regFarmerRecordsList;

````
38. Add below snippet in scheduler class method responseScheduler()  if condition.
````
  {
                String protocol = cacheDTO.getProtocol();
                    RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());
                    MsgTrackerEntity msgTrackerEntity = txnTrackerDbService.saveRequestDetails(requestDTO, protocol);
                    List<QueryDTO> queryDTOList = msgTrackerEntity.getTxnTrackerEntityList().stream()
                            .map(txnTrackerEntity -> {
                                try {
                                    return objectMapper.readValue(txnTrackerEntity.getQuery(), QueryDTO.class);
                                } catch (JsonProcessingException e) {
                                    return null;
                                }
                            }).toList();
                    List<String> refRecordsStringsList = farmerResponseBuilderService.getRegFarmerRecords(queryDTOList);
                    G2pcError g2pcError = responseBuilderService.buildOnSearchScheduler(refRecordsStringsList , cacheDTO);
                    log.info("on-search database updation response from sunbird - "+g2pcError.getCode());
                    if (!g2pcError.getCode().equals(HttpStatus.OK.toString())) {
                        throw new G2pHttpException(g2pcError);
                    } else {
                        txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    }
                    
                }
````
39. Add below the catch statement at last as mentioned in point 33 that try is already written.
````
catch ( G2pHttpException e){
   log.error("Exception thrown from on-search endpoint"+ e.getG2PcError().getMessage());
}
catch (Exception ex) {
   log.error("Exception in responseScheduler: {}", ex.getMessage());
}

````
40. This scheduler will run every 1 min. But to test this code write a test case in Test class.
````
@Test
void testResponseScheduler() throws IOException {
   scheduler.responseScheduler();
}
````
41. Create new method in RegistryController for /status 
````
 @Operation(summary = "Receive status request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/txn/status")
    public AcknowledgementDTO handleStatusRequest(@RequestBody String requestString) throws Exception { 
    }
````
42. Add below code for authenticating user
````
dpCommonUtils.handleToken();
````
43. Add below code for validating the statusRequest and updating cache. 
````
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class, HeaderDTO.class);

        StatusRequestDTO statusRequestDTO = objectMapper.readerFor(StatusRequestDTO.class).
                readValue(requestString);
        StatusRequestMessageDTO statusRequestMessageDTO = null;

        Map<String, Object> metaData = (Map<String, Object>) statusRequestDTO.getHeader().getMeta().getData();

        statusRequestMessageDTO = farmerValidationService.signatureValidation(metaData, statusRequestDTO);
        statusRequestDTO.setMessage(statusRequestMessageDTO);
        String cacheKey = Constants.STATUS_CACHE_KEY_STRING + statusRequestMessageDTO.getTransactionId();
        try {
            farmerValidationService.validateStatusRequestDTO(statusRequestDTO);
           return requestHandlerService.buildCacheStatusRequest(
                    objectMapper.writeValueAsString(statusRequestDTO), cacheKey,CoreConstants.SEND_PROTOCOL_HTTPS);
        }
        catch (G2pcValidationException e) {
            throw new G2pcValidationException(e.getG2PcErrorList());
        }catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    
````
44. Add below endpoint in RegistryController.java. 
````
 @GetMapping(value = "/dashboard/sftp/dp1/data", produces = "text/event-stream")
    public SseEmitter sseEmitterFirstPanel() {
        return dpSftpPushUpdateService.register();
    }
````
45. Add below overloaded method in FarmerValidationService 
````
StatusRequestMessageDTO signatureValidation(Map<String, Object> metaData, StatusRequestDTO requestDTO) throws Exception ;
````
45. Override above method in FarmerValidationServiceImpl.
````
    @Override
    public StatusRequestMessageDTO signatureValidation(Map<String, Object> metaData, StatusRequestDTO requestDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        StatusRequestMessageDTO messageDTO;
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            Resource resource = resourceLoader.getResource(farmer_key_path);
            InputStream fis = resource.getInputStream();
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = requestDTO.getMessage().toString();
                String data = requestHeaderString+messageString;
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid ->"+e.getMessage()));
                }
                catch(IOException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        log.info("Rejecting the on-search request in farmer as signature is not valid");
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(StatusRequestMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, StatusRequestMessageDTO.class);
                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = requestHeaderString+messageString;
                log.info("Signature ->"+requestSignature);
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }
                catch(IOException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = requestDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(StatusRequestMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, StatusRequestMessageDTO.class);
            }
        }
        requestDTO.setMessage(messageDTO);
        return messageDTO;
    }
````
46. Add below method to validated statusRequestMessage in FarmerValidationService.
````
void validateStatusRequestDTO (StatusRequestDTO requestDTO) throws IOException, G2pcValidationException;
````
47. Override above method in FarmerValidationServiceImpl
````
@Override
    public void validateStatusRequestDTO(StatusRequestDTO statusRequestDTO) throws IOException, G2pcValidationException {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] json = objectMapper.writeValueAsBytes(statusRequestDTO.getMessage());
        StatusRequestMessageDTO statusRequestMessageDTO =  objectMapper.readValue(json, StatusRequestMessageDTO.class);
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(statusRequestDTO.getHeader());
        RequestHeaderDTO headerDTO = objectMapper.readerFor(RequestHeaderDTO.class).
                readValue(headerString);
        requestHandlerService.validateRequestHeader(headerDTO);
        requestHandlerService.validateStatusRequestMessage(statusRequestMessageDTO);
    }
````
48. To call on-status endpoint from dc add below snippet in scheduler class in try block. Refer application zip. 
    1. Get cache key from redis stored with start of key "status-request-farmer*".
    2. Iterate list of cache.
    3. Get request data for particular cache key and convert it into cacheDTO.
    4. Check if status is pending for that data.
    5. Get StatusRequestDto from cacheDto and statusRequestMessageDTO from StatusRequestDto.
    6. Fetch the msgTrackerEntity from db using statusRequestDto and build responseHeaderDto.
    7. Build StatusResponseMessageDto using StatusRequestMessageDto.
    8. Build StatusResponseString , create resource using farmer key path and send response to dc. 
````
   List<String> statusCacheKeysList = txnTrackerRedisService.getCacheKeys(Constants.STATUS_CACHE_KEY_SEARCH_STRING);
            for (String cacheKey : statusCacheKeysList) {
                String requestData = txnTrackerRedisService.getRequestData(cacheKey);
                CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(requestData);
                if (cacheDTO.getStatus().equals(HeaderStatusENUM.PDNG.toValue())) {
                    StatusRequestDTO statusRequestDTO = objectMapper.readerFor(StatusRequestDTO.class).readValue(cacheDTO.getData());
                    StatusRequestMessageDTO statusRequestMessageDTO = objectMapper.convertValue(statusRequestDTO.getMessage(), StatusRequestMessageDTO.class);
                    G2pcError g2pcError = responseBuilderService.buildOnStatusScheduler(cacheDTO);
                    log.info("on-status database updation response from sunbird - "+g2pcError.getCode());
                    if (!g2pcError.getCode().equals(HttpStatus.OK.toString())) {
                        throw new G2pHttpException(g2pcError);
                    } else {
                        txnTrackerDbService.updateMessageTrackerStatusDb(statusRequestMessageDTO.getTransactionId());
                        txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    }
                }
            }
```` 
49. Create DpDashboardController for creating dashboard endpoints for Grafana.
````
package g2pc.ref.farmer.regsvc.controller.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DpDashboardController {

    @Value("${dashboard.dp_dashboard_url}")
    private String dpDashboardUrl;

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        model.addAttribute("dp_dashboard_url", dpDashboardUrl);
        return "dashboard";
    }
}

````
50. Add CorsConfig.java in config folder, refer below code.
````
package g2pc.ref.farmer.regsvc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${dashboard.cors_origin_url}")
    private String corsOriginUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsOriginUrl)
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
````
51. Add below dto SftpDpData in DP.
````
package g2pc.ref.farmer.regsvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SftpDpData {

    private String dpType;
    private String messageTs;
    private String transactionId;
    private String fileName;
}

````
52. Add below interface in service package 
````
package g2pc.ref.farmer.regsvc.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DpSftpPushUpdateService {

    SseEmitter register();

    void pushUpdate(Object update);
}
````
53. Implement service in below class.
````
package g2pc.ref.farmer.regsvc.serviceimpl;

import g2pc.ref.farmer.regsvc.dto.SftpDpData;
import g2pc.ref.farmer.regsvc.service.DpSftpPushUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DpSftpPushUpdateServiceImpl implements DpSftpPushUpdateService {
    private final List<SseEmitter> emitters = new ArrayList<>();
    public SseEmitter register() {
        int minutes = 15;
        long timeout = (long) minutes * 60000;
        SseEmitter emitter = new SseEmitter(timeout);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        log.info("SSE emitter registered" + emitter);
        return emitter;
    }
    public void pushUpdate(Object update) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(update);
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }
    public SftpDpData getSftpDpData(String dpType, String messageTs, String transactionId, String filename){
        SftpDpData sftpDpData = new SftpDpData();
        sftpDpData.setDpType(dpType);
        sftpDpData.setMessageTs(messageTs);
        sftpDpData.setTransactionId(transactionId);
        sftpDpData.setFileName(filename);
        return sftpDpData;
    }
}

````

# 6. Data Consumer (DC) Implementation
- In DC implementation , as explained in Overview of libraries , dependency of G2pc-dc-core-lib needs to be added.
- Data consumers are  going to act as consumers as well as providers.
Implementation explained in below point when it act like data consumer -
  1. When it acts like a consumer , it needs to define an endpoint which accepts payload . Example Shown in the image below. 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/patload_postman.png)
  2. Using this data consumer will decide which data provider’s endpoint it needs to call and which request it needs to build.
  3. Once a request is created /search endpoint it will call and once positive acknowledgement is there it will save pending status in cache for particular transaction id. Refer below for more understanding.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dc-seq-diagram-2.png)
- Implementation explained in below point when it act like data provider -
  1. As shown in Figure 2 data provider needs to implement the end point and also make calls to the endpoint of the data provider. 
  2. At first Data consumer service needs to write the /on-search end-point. 
  3. This endpoint will receive a responseString transferred by the data provider. 
  4. In this endpoint authentication also needs to be defined to ensure that the correct user is accessing the endpoint or not. 
  5. Also need to make sure that the correct signature and valid message is received. 
  6. This responseString will get validated as per g2p specification. Please refer to the link mentioned and image below.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/on_search_spec.png)
  7. Once responseString gets validated data consumers should update that data in redis cache and send acknowledgement back to the data consumer. Refer below for more understanding.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dc-on-search-sequence-dia.png)

# 7. How to create a Data Consumer ? 
1. Create a spring boot application with the latest spring-boot version , maven and Java 17. And Click on generate to download.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/spring_boot_dc_creation.png)
2. Extract the downloaded jar and open it in IDE. 
3. Add below dependencies in <dependencies> tag in pom.xml
````
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.5.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>g2pc.dc.core.lib</groupId>
            <artifactId>g2pc-dc-core-library</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
            <version>2.15.0</version>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
            <version>1.6.15</version>
        </dependency>
        <dependency>
            <groupId>com.networknt</groupId>
            <artifactId>json-schema-validator</artifactId>
            <version>1.0.82</version>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>2.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-jasper</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>javax.servlet.jsp.jstl</groupId>
            <artifactId>javax.servlet.jsp.jstl-api</artifactId>
            <version>1.2.1</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.10.2</version>
        </dependency>
````
4. Create package structure shown below.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dc-package-structure.png)
5. Add .p12 files for search received from dp and on-search 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/.p12-dc.png)
5. In the config package , create the ObjectMapperConfig.java class. This class is used to avoid ambiguity between parent class and child class of Header.
````
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class);
        return objectMapper;
    }
}
````
6. Take reference of below application.yml , create application.yml for particular dc with the help of details mentioned after .yml file.
````
spring:
  mvc:
    view:
      prefix: /WEB-INF/jsp/
      suffix: .jsp

    pathmatch:
      matching-strategy: ANT_PATH_MATCHER

  datasource:
    driverClassName: org.postgresql.Driver
    # Add db name, schema name, username and password for db connection as per your postgres/mysql connection.
    url: jdbc:postgresql://{Domain-id}}:{port}/{Database name}?currentSchema={Schema name}
    username: {username of database connection}
    password: {password of database connection}

    hikari:
      data-source-properties:
        stringtype: unspecified
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        maintainTimeStats: false
        maximum-pool-size: 5
        connection-timeout: 5000
  jpa:
    properties:
      hibernate:
        jdbc:
          lob:
            non_contextual_creation: true
      dialect: org.hibernate.dialect.PostgreSQLDialect
    hibernate.ddl-auto: none
    show-sql: false
    open-in-view: false
    generate-ddl: false
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration

  devtools:
    restart:
      additional-paths: src/main/webapp
      exclude: static/**,public/**

server:
  port: {add port as per requirement}
  error:
    include-message: always

# This is redis connection configuration
spring.data.redis:
  repositories.enabled: false
  host: {host domain of redis connection}
  password: {password of redis port}
  port: {port of redis connection}

keycloak:
  # These configurations are given by dc that is the reason name is from_dp. 
  from_dp:
    # this will be many dp , add url , clientid and client secret for every dp.
    {dp-name}:
      url: "https://{domain of dc instance}/auth/realms/{realm name of dp}/protocol/openid-connect/token"
      clientId: {client id of dp given by dp}
      clientSecret: {client secret of dp client}
  dc:
    url: https://{domain of dc instance}/auth
    username: {username of dc instance}
    password: {password of dc instance}
    master:
      # master token url for particular dc keycloak, add domain name http://127.0.0.1:8081/ 
      url: https://{domain}/auth/realms/master/protocol/openid-connect/token
      getClientUrl: https://{domain}/auth/admin/realms/{client id of dp}/clients
      clientId: {client name given to admin client created in dc instance}
      clientSecret: {client secret of admin client from dc keycloak instance. Refer 8.7 for same}
    client:
      # dc client token url. Add domain name and realm-id
      url: https://{domain}/auth/realms/{realm-id}/protocol/openid-connect/token
      realm: {realm id}
      clientId: {dc client id}
      clientSecret: {client secret of client from dc keycloak instance. Refer 8.7 for same}

crypto:
  to_dp_{dp_name}:
    # flag of encryption (use only small case)    
    support_encryption: true
    # flag of signature (use only small case)
    support_signature: true
    password: {password of search .p12 file of particuler dp}
    key_path: {keypath of search .p12 file of particuler dp}
  from_dp_{dp_name}:
    # flag of encryption (use only small case)    
    support_encryption: true
    # flag of signature (use only small case)
    support_signature: true
    password: {password of on-search .p12 file}
    key_path: {keypath of on-search .p12 file}
    id: {dp id which will be common between dc and dp}

registry:
  api_urls:
    {dp-name}_search_api: "http://{host}:{port}/private/api/v1/registry/search"
    {dp-name}_status_api: "http://{host}:{port}/private/api/v1/registry/txn/status"

dashboard:
  left_panel_url: "https://{domain of dc}/grafana/d-solo/cb26f39f-97f3-43ea-9f42-68d49d9822a3/left-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  right_panel_url: "https://{domain of dc}/grafana/d-solo/d9f9c625-934b-4a65-995f-c742daad6387/right-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  bottom_panel_url: "https://{domain of dc}/grafana/d-solo/a25a6c65-fda7-4fdd-80a7-80442aed17e8/bottom-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  post_endpoint_url: "http://localhost:8000/public/api/v1/consumer/search/csv"
  clear_dc_db_endpoint_url: "http://{host}:{port of dc}/private/api/v1/registry/clear-db"
  clear_dp1_db_endpoint_url: "http://{host}:{port of dp1}/private/api/v1/registry/clear-db"
  # Add all respective clear-db endpoint of all dp example is above. 
  left_panel_data_endpoint_url: "http://{host}:{port of dc}/dashboard/leftPanel/data"
  sftp_post_endpoint_url: "http://{host}:{port of dc}/public/api/v1/consumer/search/sftp/csv"
  sftp_dc_data_endpoint_url: "http://{host}:{port of dc}/dashboard/sftp/dc/data"
  sftp_dp1_data_endpoint_url: "http://{host}:{port of dp1}/dashboard/sftp/dp1/data"
   # Add all respective sftp_dp1_data_endpoint_url endpoint of all dp example is above. 
  dc_status_endpoint_url: "http://{host}:{port of dc}/private/api/v1/consumer/status/payload?transactionType=search&transactionId="
  sftp_left_panel_url: "https://{domain of dc}/grafana/d-solo/aa62b4d5-f0c6-4c5d-97eb-753343c89a32/sftp-left-panel-data?orgId=1&refresh=5s&from=1705573550702&to=1705595150703&panelId=1"
  sftp_right_panel_url: "https://{domain of dc}/grafana/d-solo/c319354b-d0a9-4541-ae9f-d052e31fa275/sftp-right-panel-data?orgId=1&refresh=5s&from=1705574488336&to=1705596088337&panelId=1"
  sftp_bottom_panel_url: "https://{domain of dc}/grafana/d-solo/c63fe588-c69c-4918-bb96-97fba722afc8/sftp-bottom-panel-data?orgId=1&refresh=5s&from=1705574366440&to=1705595966440&panelId=1"
# sftp connection configurations. 
sftp:
  listener:
    # hostname of sftp connection of dp e.g. localhost 
    host: {host name}
    port: {port of sftp connection of dc }
    user: {username of sftp connection of dc }
    password: {password of sftp connection of dc }
    remote:
      # path of sftp client for inbound e.g. /inbound 
      inbound_directory: {path mentioned in sftp client for inbound}
      outbound_directory: {path mentioned in sftp client for outbound}
    local:
      inbound_directory: {path created in local machine for inbound}
      outbound_directory: {path created in local machine for inbound}
  
  # add dp as per requirement and add all these fields
  dp1:
    # hostname of sftp connection of dp e.g. localhost 
    host: {host name}
    port: {port of sftp connection of dp }
    user: {username of sftp connection of dp }
    password: {password of sftp connection of dp }
    remote:
      # path of sftp client for inbound e.g. /inbound 
      inbound_directory: {path mentioned in sftp client for inbound}
sunbird:
  save:
    response_data: http://{host}:{port of sunbird}/api/v1/Response_Data
    response_tracker: http://{host}:{port of sunbird}/api/v1/Response_Tracker
  enabled: true
  elasticsearch:
    host: {domain of elasticsearch like if running on local env i.e localhost} 
    port: {port mention in docker-compose for elastic search}
    scheme: http
````
8. Add RegistryConfig.java class in config package 
````
@Service
@Slf4j
public class RegistryConfig {}
````
9. Add below values which mentioned in application.yml for particular dp or multiple dps
````

    @Value("${registry.api_urls.farmer_search_api}")
    private String farmerSearchURL;

    @Value("${registry.api_urls.mobile_search_api}")
    private String mobileSearchURL;

    @Value("${dashboard.clear_dp1_db_endpoint_url}")
    private String farmerClearDbURL;

    @Value("${dashboard.clear_dp2_db_endpoint_url}")
    private String mobileClearDbURL;

    @Value("${keycloak.from_dp.farmer.clientId}")
    private String farmerClientId;

    @Value("${keycloak.from_dp.farmer.clientSecret}")
    private String farmerClientSecret;

    @Value("${keycloak.from_dp.farmer.url}")
    private String keycloakFarmerTokenUrl;

    @Value("${keycloak.from_dp.mobile.url}")
    private String keycloakMobileTokenUrl;

    @Value("${keycloak.from_dp.mobile.clientId}")
    private String mobileClientId;

    @Value("${keycloak.from_dp.mobile.clientSecret}")
    private String mobileClientSecret;

    @Value("${crypto.to_dp_farmer.support_encryption}")
    private boolean isFarmerEncrypt;

    @Value("${crypto.to_dp_farmer.support_signature}")
    private boolean isFarmerSign;

    @Value("${crypto.to_dp_mobile.support_encryption}")
    private boolean isMobileEncrypt;

    @Value("${crypto.to_dp_mobile.support_signature}")
    private boolean isMobileSign;

    @Value("${crypto.to_dp_mobile.key_path}")
    private String mobileKeyPath;

    @Value("${crypto.to_dp_farmer.key_path}")
    private String farmerKeyPath;

    @Value("${crypto.to_dp_farmer.password}")
    private String farmerKeyPassword;

    @Value("${crypto.to_dp_mobile.password}")
    private String mobileKeyPassword;

    @Value("${sftp.dp1.host}")
    private String sftpDp1Host;

    @Value("${sftp.dp1.port}")
    private int sftpDp1Port;

    @Value("${sftp.dp1.user}")
    private String sftpDp1User;

    @Value("${sftp.dp1.password}")
    private String sftpDp1Password;

    @Value("${sftp.dp1.remote.inbound_directory}")
    private String sftpDp1RemoteInboundDirectory;

    @Value("${sftp.dp2.host}")
    private String sftpDp2Host;

    @Value("${sftp.dp2.port}")
    private int sftpDp2Port;

    @Value("${sftp.dp2.user}")
    private String sftpDp2User;

    @Value("${sftp.dp2.password}")
    private String sftpDp2Password;

    @Value("${sftp.dp2.remote.inbound_directory}")
    private String sftpDp2RemoteInboundDirectory;

    @Value("${crypto.sample.password}")
    private String dummyKeyPassword;

    @Value("${crypto.sample.key.path}")
    private String dummyKeyPath;

    @Value("${registry.api_urls.farmer_status_api}")
    private String farmerStatusUrl;

    @Value("${registry.api_urls.mobile_status_api}")
    private String mobileStatusUrl;

````
10. Add below method getFarmerRegistryMap() of particular dp and create same methods for multiple dps.
````
  private Map<String, String> getFarmerRegistryMap() {
       Map<String, String> farmerRegistryMap = new HashMap<>();
        farmerRegistryMap.put(CoreConstants.QUERY_NAME, "paid_farmer");
        farmerRegistryMap.put(CoreConstants.REG_TYPE, "ns:FARMER_REGISTRY");
        farmerRegistryMap.put(CoreConstants.REG_SUB_TYPE, "");
        farmerRegistryMap.put(CoreConstants.QUERY_TYPE, "namedQuery");
        farmerRegistryMap.put(CoreConstants.SORT_ATTRIBUTE, "farmer_id");
        farmerRegistryMap.put(CoreConstants.SORT_ORDER, SortOrderEnum.ASC.toValue());
        farmerRegistryMap.put(CoreConstants.PAGE_NUMBER, "1");
        farmerRegistryMap.put(CoreConstants.PAGE_SIZE, "10");
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_URL, keycloakFarmerTokenUrl);
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_ID, farmerClientId);
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_SECRET, farmerClientSecret);
        farmerRegistryMap.put(CoreConstants.SUPPORT_ENCRYPTION, "" + isFarmerEncrypt);
        farmerRegistryMap.put(CoreConstants.SUPPORT_SIGNATURE, "" + isFarmerSign);
        if(isSignEncrypt.equals("2")){
            farmerRegistryMap.put(CoreConstants.KEY_PATH, dummyKeyPath);
            farmerRegistryMap.put(CoreConstants.KEY_PASSWORD, dummyKeyPassword);
        } else {
            farmerRegistryMap.put(CoreConstants.KEY_PATH, farmerKeyPath);
            farmerRegistryMap.put(CoreConstants.KEY_PASSWORD, farmerKeyPassword);
        }
        farmerRegistryMap.put(CoreConstants.DP_SEARCH_URL, farmerSearchURL);
        farmerRegistryMap.put(CoreConstants.DP_CLEAR_DB_URL, farmerClearDbURL);
        farmerRegistryMap.put(SftpConstants.SFTP_HOST, sftpDp1Host);
        farmerRegistryMap.put(SftpConstants.SFTP_PORT, String.valueOf(sftpDp1Port));
        farmerRegistryMap.put(SftpConstants.SFTP_USER, sftpDp1User);
        farmerRegistryMap.put(SftpConstants.SFTP_PASSWORD, sftpDp1Password);
        farmerRegistryMap.put(SftpConstants.SFTP_SESSION_CONFIG, "no");
        farmerRegistryMap.put(SftpConstants.SFTP_ALLOW_UNKNOWN_KEYS, String.valueOf(true));
        farmerRegistryMap.put(SftpConstants.SFTP_REMOTE_INBOUND_DIRECTORY, sftpDp1RemoteInboundDirectory);
        farmerRegistryMap.put(CoreConstants.DP_STATUS_URL , farmerStatusUrl);

        return farmerRegistryMap;
    }
````
11. Add below 2 methods to create map of particular dp. 
    -   In getQueryParamsConfig() create map for particular dp for specific queryparams declared in specification and add it in queryParam map.
    -   In getRegistrySpecificConfig() in this method particular dp's map creation method will call and map will created with particular dp key and returned.
````
 public Map<String, Object> getQueryParamsConfig() {
        Map<String, Object> queryParamsConfig = new HashMap<>();

        Map<String, String> farmerRegistryMap = new HashMap<>();
        farmerRegistryMap.put("farmer_id", "");
        farmerRegistryMap.put("season", "");

        queryParamsConfig.put(Constants.FARMER_REGISTRY, farmerRegistryMap);
        return queryParamsConfig;
    }

    /**
     * Map to represent which common values to be used to generate request for a registry
     *
     * @return Map to represent registry specific config values
     */
    public Map<String, Object> getRegistrySpecificConfig() {
        Map<String, Object> queryParamsConfig = new HashMap<>();

        Map<String, String> farmerRegistryMap = getFarmerRegistryMap();

        queryParamsConfig.put(Constants.FARMER_REGISTRY, farmerRegistryMap);
        return queryParamsConfig;
    }
````

12. Create DcController in controller.rest package
````
@RestController
@Slf4j
@RequestMapping(produces = "application/json")
@Tag(name = "Data Consumer", description = "DC APIs")
public class DcController {}
````
13. Create below entrypoint for triggering dc communication using only one data.
````
    @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
    @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
    @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
    @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/public/api/v1/consumer/search/payload")
    public AcknowledgementDTO createSearchRequestsFromPayload(@RequestBody Map<String, Object> payloadMap) throws Exception {
    log.info("Payload received from postman");
    AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
    if (ObjectUtils.isNotEmpty(payloadMap)) {
    acknowledgementDTO = dcRequestBuilderService.generateRequest(Collections.singletonList(payloadMap),
                    CoreConstants.SEND_PROTOCOL_HTTPS, "", "","");
    }
    return acknowledgementDTO;
    }
````
15. To run above entrypoint refer below curl.
````
curl --location 'http://localhost:8000/public/api/v1/consumer/search/payload' \
--header 'Content-Type: application/json' \
--data '{
		"farmer_id" : "F-1",
		"farmer_name" : "Farmer-1",
		"mobile_number" : "9767670153",
        "season" : "2023-xyz"
    
}'

````
16. Create below entry point for triggering dc communication for multiple data using csv file.
````
 @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/consumer/search/csv")
    public AcknowledgementDTO createSearchRequestsFromCsv(@RequestPart(value = "file") MultipartFile payloadFile) throws Exception {
     log.info("Payload received from csv file");
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        if (ObjectUtils.isNotEmpty(payloadFile)) {
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), "payload.csv");
            if (Files.exists(tempFile)) {
                commonUtils.deleteFolder(tempFile);
            }
            Files.createFile(tempFile);
            payloadFile.transferTo(tempFile.toFile());
            acknowledgementDTO = dcRequestBuilderService.generateRequest(
                    requestBuilderService.generatePayloadFromCsv(tempFile.toFile()), CoreConstants.SEND_PROTOCOL_HTTPS,
                    dcRequestBuilderService.demoTestEncryptionSignature(tempFile.toFile()),
                    payloadFile.getName(), "");
            Files.delete(tempFile);
        }
        return acknowledgementDTO;
    }
````
17. To run above entrypoint refer below curl and create one payload.csv with multiple data.
````
curl --location 'localhost:8000/private/api/v1/consumer/search/csv' \
--form 'file=@"/home/ttpl-rt-119/Downloads/payload.csv"'
````
18. Create below endpoint for clearing db in dc.
````
 @GetMapping("/public/api/v1/registry/clear-db")
    public void clearDb() throws G2pHttpException, IOException {
     commonUtils.handleToken();
        responseDataRepository.deleteAll();
        log.info("DC DB cleared");

        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig("").entrySet()) {
            try {
                Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig("").get(configEntryMap.getKey());
                String jwtToken = requestBuilderService.getValidatedToken(registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString());
                log.info("jwtToken: {}", jwtToken);
                log.info("url: {}", registrySpecificConfigMap.get(CoreConstants.DP_CLEAR_DB_URL).toString());
                HttpResponse<String> response = g2pUnirestHelper.g2pGet(registrySpecificConfigMap.get(CoreConstants.DP_CLEAR_DB_URL).toString())
                        .header("Content-Type", "application/json")
                        .header("Authorization", jwtToken)
                        .asString();
                log.info("DP " + registrySpecificConfigMap.get(CoreConstants.REG_TYPE) + " DB cleared with response " + response.getStatus());
            } catch (Exception e) {
                log.error("Exception in clearDb: ", e);
            }
        }
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("DC Redis cache cleared");
    }
````
19. Add below exception handling in the DC controller.
````
@ExceptionHandler(value = G2pcValidationException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ValidationErrorResponse handleValidationException(G2pcValidationException ex) {
    return new ValidationErrorResponse(ex.getG2PcErrorList());
}

@ExceptionHandler(value = G2pHttpException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ErrorResponse handleG2pHttpStatusException(G2pHttpException ex) {
    return new ErrorResponse(ex.getG2PcError());
}
````
19. Create RegResponseDTO for particular data provider in dto.(dp-name) package. Shown below 
````
package g2pc.ref.dc.client.dto.farmer.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegRecordFarmerDTO {

    @JsonProperty("farmer_id")
    private String farmerId;

    @JsonProperty("farmer_name")
    private String farmerName;

    @JsonProperty("season")
    private String season;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("payment_date")
    private String paymentDate;

    @JsonProperty("payment_amount")
    private Double paymentAmount;
}
````
20. Declare below interface DcRequestBuilderService to generateRequest for single data and multiple data in csv file.
````
public interface DcRequestBuilderService {

   AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList, String protocol,
                                       String isSignEncrypt, String payloadFilename, String inboundFilename) throws Exception;

    AcknowledgementDTO generateStatusRequest(String transactionID, String transactionType, String protocol) throws Exception;
}
````
21. Create DcRequestBuilderServiceImpl.java class and implement it from DcRequestBuilderService interface.
````
@Service
@Slf4j
public class DcRequestBuilderServiceImpl implements DcRequestBuilderService {
````
22. Add below autowired beans in above class.
````
    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    RegistryConfig registryConfig;

    @Autowired
    TxnTrackerService txnTrackerService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    ResponseTrackerRepository responseTrackerRepository;

    @Autowired
    private ElasticsearchService elasticsearchService;
````
23. Override below method generateRequest() from interface.
    1. Fetch queryMapList using payload list provided and registryconfig.
    2. In for loop iterate configEntryMap fetching from registryConfig.
    3. Filter out queryMapFilteredList using registryType.
    4. Build searchCriteriaDto by calling dc-core method by passing queryParams and registrySpecificationConfig.
    5. Generate transaction id.
    6. Build searchString using method buildRequest from dc-core.
    7. Check if request is http or sftp.
    8. In http call sendRequest method by passing all arguments.
    9. Put returned g2pcError in g2pcErrorMap.
    10. If request is sftp , create SftpServerConfigDTO.
    11. Invoke sendRequestSftp method and put returned g2pcError in g2pcErrorMap.
    12. Save initial transaction and requestString in redis.
    13. Save data in db.
````
   public AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList, String protocol,
                                              String isSignEncrypt, String payloadFilename, String inboundFilename) throws Exception {
        Map<String, G2pcError> g2pcErrorMap = new HashMap<>();
        List<Map<String, Object>> queryMapList = requestBuilderService.createQueryMap(payloadMapList, registryConfig.getQueryParamsConfig().entrySet());
        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig(isSignEncrypt).entrySet()) {
            List<Map<String, Object>> queryMapFilteredList = queryMapList.stream()
                    .map(map -> map.entrySet().stream()
                            .filter(entry -> entry.getKey().equals(configEntryMap.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).toList();
            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig(isSignEncrypt).get(configEntryMap.getKey());
            List<SearchCriteriaDTO> searchCriteriaDTOList = new ArrayList<>();
            for (Map<String, Object> queryParamsMap : queryMapFilteredList) {
                SearchCriteriaDTO searchCriteriaDTO = requestBuilderService.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap);
                searchCriteriaDTOList.add(searchCriteriaDTO);
            }
            String transactionId = CommonUtils.generateUniqueId("T");
            String requestString = requestBuilderService.buildRequest(searchCriteriaDTOList, transactionId, ActionsENUM.SEARCH);
            String encryptedSalt = "";
            G2pcError g2pcError = new G2pcError();
            switch (isSignEncrypt) {
                case "0":
                    break;
                case "1":
                    encryptedSalt = "salt";
                case "2":
                    break;
            }
            try {
                if (protocol.equals(CoreConstants.SEND_PROTOCOL_HTTPS)) {
                    Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());

                    InputStream fis = resource.getInputStream();
                    g2pcError = requestBuilderService.sendRequest(requestString,
                            registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                            fis, encryptedSalt,
                            registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), CoreConstants.SEARCH_TXN_TYPE);
                    g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);
                    log.info("DP_SEARCH_URL = {}", registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString());
                } else if (protocol.equals(CoreConstants.SEND_PROTOCOL_SFTP)) {
                    SftpServerConfigDTO sftpServerConfigDTO = new SftpServerConfigDTO();
                    sftpServerConfigDTO.setUser(registrySpecificConfigMap.get(SftpConstants.SFTP_USER).toString());
                    sftpServerConfigDTO.setHost(registrySpecificConfigMap.get(SftpConstants.SFTP_HOST).toString());
                    sftpServerConfigDTO.setPort(Integer.parseInt(registrySpecificConfigMap.get(SftpConstants.SFTP_PORT).toString()));
                    sftpServerConfigDTO.setPassword(registrySpecificConfigMap.get(SftpConstants.SFTP_PASSWORD).toString());
                    sftpServerConfigDTO.setStrictHostKeyChecking(registrySpecificConfigMap.get(SftpConstants.SFTP_SESSION_CONFIG).toString());
                    sftpServerConfigDTO.setRemoteInboundDirectory(registrySpecificConfigMap.get(SftpConstants.SFTP_REMOTE_INBOUND_DIRECTORY).toString());

                    Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                    InputStream fis = resource.getInputStream();
                    inboundFilename = UUID.randomUUID() + ".json";
                    g2pcError = requestBuilderService.sendRequestSftp(requestString,
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                            fis, encryptedSalt,
                            registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), CoreConstants.SEARCH_TXN_TYPE,
                            sftpServerConfigDTO, inboundFilename);
                    g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);
                    if (g2pcError != null && g2pcError.getCode().contains("err")) {
                        log.info("Uploaded failed for : {}", registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                        throw new Exception("Uploaded failed for : " + registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                    } else {
                        log.info("Uploaded to inbound of : {}", registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                    }
                }
                txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue(), protocol);
                txnTrackerService.saveRequestTransaction(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), transactionId, protocol);
                txnTrackerService.saveRequestInDB(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), protocol, g2pcError,
                        payloadFilename, inboundFilename);
            } catch (Exception e) {
                log.error(Constants.GENERATE_REQUEST_ERROR_MESSAGE + ": {}", e.getMessage());
            }
        }
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(g2pcErrorMap);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        return acknowledgementDTO;
    }
````
24. Create DcSftpListener.java to handle inbound and outbound messages in package g2pc.ref.dc.client.controller.sftp
    1. Values added in this class are inbound and outbound file path mentioned in application.yml.
    2. handleMessageInbound() method written to handle inbound message.
````
package g2pc.ref.farmer.regsvc.controller.sftp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.dto.SftpDpData;
import g2pc.ref.farmer.regsvc.service.DpSftpPushUpdateService;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Configuration
@Slf4j
public class DcSftpListener {

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpLocalDirectoryInbound;


    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    FarmerValidationService farmerValidationService;

    @SuppressWarnings("unchecked")
    @ServiceActivator(inputChannel = "sftpInbound")
    public void handleMessageInbound(Message<File> message) {
        try {
            File file = message.getPayload();
            log.info("Received Message from inbound directory of dp-1: {}", file.getName());
            if (ObjectUtils.isNotEmpty(file) && file.getName().contains(".json")) {
                String requestString = new String(Files.readAllBytes(file.toPath()));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerSubtypes(RequestHeaderDTO.class,
                        ResponseHeaderDTO.class, HeaderDTO.class);

                RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                        readValue(requestString);
                RequestMessageDTO messageDTO;

                Map<String, Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();

                messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
                requestDTO.setMessage(messageDTO);
                String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
                try {
                    farmerValidationService.validateRequestDTO(requestDTO);
                    requestHandlerService.buildCacheRequest(
                            objectMapper.writeValueAsString(requestDTO), cacheKey, CoreConstants.SEND_PROTOCOL_SFTP);
                } catch (G2pcValidationException e) {
                    throw new G2pcValidationException(e.getG2PcErrorList());
                } catch (JsonProcessingException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
            Files.deleteIfExists(Path.of(sftpLocalDirectoryInbound + "/" + file.getName()));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(Message<?> message) {
        Throwable error = (Throwable) message.getPayload();
        log.error("Handling ERROR: {}", error.getMessage());
    }
}

````
25. Create on-search endpoint , refer below snippet ,there are methods called in this methods refer code after this point.
````
    @Operation(summary = "Listen to registry response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.ON_SEARCH_RESPONSE_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/on-search")
    public AcknowledgementDTO handleOnSearchResponse(@RequestBody String responseString) throws Exception {
        commonUtils.handleToken();
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        ResponseDTO responseDTO = objectMapper.readerFor(ResponseDTO.class).
                readValue(responseString);
        ResponseMessageDTO messageDTO;
        Map<String, Object> metaData = (Map<String, Object>) responseDTO.getHeader().getMeta().getData();
        messageDTO = dcValidationService.signatureValidation(metaData, responseDTO);
        responseDTO.setMessage(messageDTO);
        try {
            dcValidationService.validateResponseDto(responseDTO);
            if (ObjectUtils.isNotEmpty(responseDTO)) {
                acknowledgementDTO = dcResponseHandlerService.getResponse(responseDTO);
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return acknowledgementDTO;
    }
````
26. Create DcCommonUtil.java in util package and create handle token method to handle and validate token.
````
package g2pc.ref.dc.client.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DcCommonUtils {  
}

````
26. Add below values from application.yml to validate token
````
  @Autowired
    G2pTokenService g2pTokenService;

    @Value("${keycloak.dc.client.realm}")
    private String keycloakRealm;

    @Value("${keycloak.dc.client.url}")
    private String keycloakURL;

    @Value("${keycloak.dc.master.url}")
    private String masterUrl;

    @Value("${keycloak.dc.master.getClientUrl}")
    private String getClientUrl;

    @Value("${keycloak.dc.client.clientId}")
    private String dcClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String dcClientSecret;

    @Value("${keycloak.dc.master.clientId}")
    private String masterClientId;

    @Value("${keycloak.dc.master.clientSecret}")
    private String masterClientSecret;

    @Value("${keycloak.dc.username}")
    private String adminUsername;

    @Value("${keycloak.dc.password}")
    private String adminPassword;

    @Value("${sftp.listener.host}")
    private String sftpDcHost;

    @Value("${sftp.listener.port}")
    private int sftpDcPort;

    @Value("${sftp.listener.user}")
    private String sftpDcUser;

    @Value("${sftp.listener.password}")
    private String sftpDcPassword;

    @Value("${sftp.listener.remote.inbound_directory}")
    private String sftpDcRemoteInboundDirectory;

    @Value("${sftp.listener.remote.outbound_directory}")
    private String sftpDcRemoteOutboundDirectory;

    @Value("${sftp.listener.local.inbound_directory}")
    private String sftpDcLocalInboundDirectory;

    @Value("${sftp.listener.local.outbound_directory}")
    private String sftpDcLocalOutboundDirectory;

````
27. Add below methods in DcCommonUtils.java to handle and validate token and to get sftp configuration for dc.
````
    public void handleToken() throws G2pHttpException, JsonProcessingException {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, dcClientId, dcClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterUrl, getClientUrl, g2pTokenService.decodeToken(token),
                masterClientId, masterClientSecret, adminUsername, adminPassword)) {
            throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
    }
    
     public SftpServerConfigDTO getSftpConfigForDc() {
        SftpServerConfigDTO sftpServerConfigDTO = new SftpServerConfigDTO();
        sftpServerConfigDTO.setHost(sftpDcHost);
        sftpServerConfigDTO.setPort(sftpDcPort);
        sftpServerConfigDTO.setUser(sftpDcUser);
        sftpServerConfigDTO.setPassword(sftpDcPassword);
        sftpServerConfigDTO.setAllowUnknownKeys(true);
        sftpServerConfigDTO.setStrictHostKeyChecking("no");
        sftpServerConfigDTO.setRemoteInboundDirectory(sftpDcRemoteInboundDirectory);
        sftpServerConfigDTO.setRemoteOutboundDirectory(sftpDcRemoteOutboundDirectory);
        sftpServerConfigDTO.setLocalInboundDirectory(sftpDcLocalInboundDirectory);
        sftpServerConfigDTO.setLocalOutboundDirectory(sftpDcLocalOutboundDirectory);
        return sftpServerConfigDTO;
    }
     public void deleteFolder(Path path) {
        try {
            if (Files.isRegularFile(path)) {
                Files.delete(path);
                return;
            }
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(p -> p.compareTo(path) != 0).forEach(p -> deleteFolder(p)); // delete all the children folders or files;
                Files.delete(path); // delete the folder itself;
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }
````
28. Create DcValidationService interface with 3 methods , validateRegRecords() this method is dc specific , to validate query params.
````
@Service
public interface DcValidationService {

    public void validateResponseDto(ResponseDTO responseDTO) throws Exception;

    ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception;

    StatusResponseMessageDTO signatureValidation(Map<String, Object> metaData, StatusResponseDTO statusResponseDTO) throws Exception;

    void validateStatusResponseDTO(StatusResponseDTO statusResponseDTO) throws IOException, G2pcValidationException;
}
````
29. Create DcValidationServiceImpl class to implement DcValidationService interface and override method.
````
@Service
@Slf4j
public class DcValidationServiceImpl implements DcValidationService {
````
30. Add below autowired beans in DcValidationServiceImpl. Add below values for all required dps.
````
    @Autowired
    ResponseHandlerService responseHandlerService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;


    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${crypto.from_dp_farmer.support_encryption}")
    private boolean isFarmerEncrypt;

    @Value("${crypto.from_dp_farmer.support_signature}")
    private boolean isFarmerSign;

    @Value("${crypto.from_dp_farmer.password}")
    private String farmerp12Password;

    @Value("${crypto.from_dp_farmer.key_path}")
    private String farmerKeyPath;

    @Value("${crypto.from_dp_farmer.id}")
    private String farmerID;
````
31. Override below signatureValidation() method and add required if conditions for required dps.
    1. Check get string from CoreConstants.DP_ID for respective farmer ids , and add respective attributes.
    2. Check isSign flag , if yes check metadata given from controller is true or false , if not true throw error that configurations are not valid.
    2. Take farmerKeyPath for dp .p12 file.
    3. Check isEncrypt flag , if true check isMsgEncrypt flag from header is true or false , if not true throw error that configurations are not valid.
    4. Recreate signature using header and message. Verify both signature if error from verifySignature throw error signature invalid.
    5. Decrypt message , if successfully decrypted add in requestDto.
    6. Above logic is same in 4 cases of signature and encryption mentioned "Overview / List of libraries".

````
  @Override
    public ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception {


        String p12Password ="";
        boolean isEncrypt = false;
        boolean isSign=false;
        String keyPath="";
        if(metaData.get(CoreConstants.DP_ID).equals(farmerID)){
            p12Password = farmerp12Password;
            isEncrypt = isFarmerEncrypt;
            isSign = isFarmerSign;
            keyPath = farmerKeyPath;
        } else if(metaData.get(CoreConstants.DP_ID).equals(mobileID)){
            p12Password = mobilep12Password;
            isEncrypt=isMobileEncrypt;
            isSign = isMobileSign;
            keyPath = mobileKeyPath;
        }
        log.info("Is encrypted ? -> "+isEncrypt);
        log.info("Is signed ? -> "+isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMessageDTO messageDTO;


        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            Resource resource = resourceLoader.getResource(keyPath);
            InputStream fis = resource.getInputStream();

            if(isEncrypt){
                if(!responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                String responseSignature = responseDTO.getSignature();
                String messageString = responseDTO.getMessage().toString();
                String data = responseHeaderString+messageString;
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature) , fis , p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                } catch(IOException e){
                    log.info("Rejecting the on-search request in signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(ResponseMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, ResponseMessageDTO.class);
                String responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                String responseSignature = responseDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = responseHeaderString+messageString;
                log.info("Signature ->"+responseSignature);
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature) , fis , p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }catch(IOException e){
                    log.info("Rejecting the on-search request in signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = responseDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(),"Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(ResponseMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, ResponseMessageDTO.class);
            }
        }
        return messageDTO;
    }
````
32. Override validateResponse() to validate ResponseDTO
````
 @Override
    public void validateResponseDto(ResponseDTO responseDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDTO.getHeader());
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerString);
        responseHandlerService.validateResponseHeader(headerDTO);
        byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
        ResponseMessageDTO messageDTO  =  objectMapper.readValue(json, ResponseMessageDTO.class);
        responseHandlerService.validateResponseMessage(messageDTO);
    }
````
33. Create DcResponseHandlerService interface to handle the response
````
public interface DcResponseHandlerService {

    AcknowledgementDTO getResponse(ResponseDTO responseDTO) throws JsonProcessingException;
}
````
34. Create DcResponseHandlerServiceImpl class which implements DcResponseHandlerService interface 
````
@Service
@Slf4j
public class DcResponseHandlerServiceImpl implements DcResponseHandlerService {

    @Autowired
    private TxnTrackerService txnTrackerService;

    @Override
    public AcknowledgementDTO getResponse(ResponseDTO responseDTO) throws JsonProcessingException {
      AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();

       G2pcError g2pcError = txnTrackerService.updateTransactionDbAndCache(responseDTO, outboundFilename);
        log.info("on-search response received from registry : {}", objectMapper.writeValueAsString(responseDTO));
        log.info("on-search database updation response from sunbird - "+g2pcError.getCode());
        if (g2pcError.getCode().equals(HttpStatus.OK.toString())){
            acknowledgementDTO.setMessage(Constants.ON_SEARCH_RESPONSE_RECEIVED.toString());
            acknowledgementDTO.setStatus(Constants.COMPLETED);

        } else {
            acknowledgementDTO.setMessage(Constants.INVALID_RESPONSE.toString());
            acknowledgementDTO.setStatus(Constants.PENDING);
            throw  new G2pHttpException(g2pcError);

        }

        return acknowledgementDTO;
    }
}
````
35. Create new entrypoint method for /status. 
````
 @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/consumer/status/payload")
    public AcknowledgementDTO createStatusRequest(@RequestParam String transactionId , @RequestParam String transactionType) throws Exception {
        log.info("Payload received from csv file");
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        if (ObjectUtils.isNotEmpty(transactionId)) {
            acknowledgementDTO = dcRequestBuilderService.generateStatusRequest(transactionId,transactionType);
        }
        return acknowledgementDTO;
    }
````
Refer below curl to execute above entrypoint. In which you can see we are sending transactionId for which we wanted to know status and action/transactionType for which we are searching status.
````
curl --location \
 --request \
 POST 'localhost:8000/private/api/v1/consumer/status/payload?transactionId=T757-5372-9253-9725-4673&transactionType=search'
````
36. Declare below method in DcRequestBuilderService interface to generate request for /status endpoint. 
````
 AcknowledgementDTO generateStatusRequest(String transactionID,String transactionType) throws Exception;
````
37. Override the above method in DcRequestBuilderServiceImpl class.
    1. Generate unique transaction id for status transaction. 
    2. Find registryType from response_tracker and response using transactionId given from postman.
    3. Fetch registry specific map from registryConfig class , as it returns values specified for registry like url and credentials.
    4. Call buildTransactionRequest() method by passing transaction id and transactionType given by postman.
    5. Invoke buildStatusRequest() method by passing txnStatusRequestDTO,statusRequestTransactionId and type of transaction. 
    6. Create resource and inputstream using keypath mentioned in application.yml.
    7. Call sendRequest() and pass dp status endpoint url, keycloak client id , client secret , url , encryption-signature flags , .p12 file password and status url flag to identify the method is called for status operation.
    8. Invoke saveInitialStatusTransaction() , to save initial transaction id dc redis.
    9. Call saveRequestTransaction() , to save requestString along transactionId.
    10. Call saveRequestInStatusDB() , to save the data in dc side tables.
````
 @Override
    public AcknowledgementDTO generateStatusRequest(String transactionID,String transactionType) throws Exception {
       AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        String statusRequestTransactionId = CommonUtils.generateUniqueId("T");
        ObjectMapper objectMapper = new ObjectMapper();
        String encryptedSalt = "";
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("transaction_id.keyword",transactionID);
        SearchResponse responseTrackerSearchResponse = elasticsearchService.exactSearch("response_tracker", fieldValues);
        if (responseTrackerSearchResponse.getHits().getHits().length > 0) {

            String responseTrackerDtoString = responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString();
            ResponseTrackerDto responseTrackerDto  = objectMapper.readerFor(ResponseTrackerDto.class).
                    readValue(responseTrackerDtoString);
            String registryType = responseTrackerDto.getRegistryType().substring(3).toLowerCase();
            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig("").get(registryType);
            TxnStatusRequestDTO txnStatusRequestDTO = requestBuilderService.buildTransactionRequest(transactionID, transactionType);
            String statusRequestString = requestBuilderService.buildStatusRequest(txnStatusRequestDTO, statusRequestTransactionId, ActionsENUM.STATUS);
            G2pcError g2pcError = null;
            try {
                Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                InputStream fis = resource.getInputStream();
                g2pcError = requestBuilderService.sendRequest(statusRequestString,
                        registrySpecificConfigMap.get(CoreConstants.DP_STATUS_URL).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                        fis, encryptedSalt,
                        registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), "status");
                log.info("" + g2pcError);
            } catch (Exception e) {
                log.error(Constants.GENERATE_REQUEST_ERROR_MESSAGE, e);
            }
            txnTrackerService.saveInitialStatusTransaction(transactionType, statusRequestTransactionId, HeaderStatusENUM.RCVD.toValue(), protocol);
            txnTrackerService.saveRequestTransaction(statusRequestString,
                    registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), statusRequestTransactionId, protocol);
            txnTrackerService.saveRequestInStatusDB(statusRequestString, registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
            acknowledgementDTO.setMessage(g2pcError);
            acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        } else {
            G2pcError g2pcError = new G2pcError();
            g2pcError.setCode(ExceptionsENUM.ERROR_REQUEST_NOT_FOUND.toValue());
            g2pcError.setMessage("Data for transaction id " + transactionID + "is not found");
            acknowledgementDTO.setMessage(g2pcError);
        }
        return acknowledgementDTO;
    }
````
38. Define the below method in DcController to create /on-status endpoint which will get call from dp side.
    1. call commonUtils.handleToken() method to authenticate user.
    2. Declare objectMapper and add neccessary dependencies.
    3. Get statusResponseDto by converting string to object using objectMapper.
    4. Get validated signature statusResponseMessageDto by call signatureValidation() method.
    5. Validated statusResponseDto as per g2p specification.
    6. call getStatusResponse(). 
````
 @SuppressWarnings("unchecked")
    @Operation(summary = "Listen to registry response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.ON_SEARCH_RESPONSE_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/on-status")
    public AcknowledgementDTO handleOnStatusResponse(@RequestBody String responseString) throws Exception {
         commonUtils.handleToken();
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        StatusResponseDTO statusResponseDTO = objectMapper.readerFor(StatusResponseDTO.class).
                readValue(responseString);
        StatusResponseMessageDTO statusResponseMessageDTO;
        Map<String, Object> metaData = (Map<String, Object>) statusResponseDTO.getHeader().getMeta().getData();
        statusResponseMessageDTO = dcValidationService.signatureValidation(metaData, statusResponseDTO);
        statusResponseDTO.setMessage(statusResponseMessageDTO);
        try {
            dcValidationService.validateStatusResponseDTO(statusResponseDTO);
            if (ObjectUtils.isNotEmpty(statusResponseDTO)) {
                acknowledgementDTO = dcResponseHandlerService.getStatusResponse(statusResponseDTO);
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return acknowledgementDTO;
    }
````
39. Add below 2 methods in DcValidationService for signatureValidation of statusResponseMessageDto and validate statusResponseDto.
````
    StatusResponseMessageDTO signatureValidation(Map<String, Object> metaData, StatusResponseDTO statusResponseDTO) throws Exception;

    void validateStatusResponseDTO(StatusResponseDTO statusResponseDTO) throws IOException, G2pcValidationException;
````
40. Override signatureValidation() method in DcValidationServiveImpl.
````
@Override
    public StatusResponseMessageDTO signatureValidation(Map<String, Object> metaData, StatusResponseDTO statusResponseDTO) throws Exception {
        String p12Password ="";
        boolean isEncrypt = false;
        boolean isSign= false;
        String keyPath="";
        if(metaData.get(CoreConstants.DP_ID).equals(farmerID)){
            p12Password = farmerp12Password;
            isEncrypt = isFarmerEncrypt;
            isSign = isFarmerSign;
            keyPath = farmerKeyPath;
        } else if(metaData.get(CoreConstants.DP_ID).equals(mobileID)){
            p12Password = mobilep12Password;
            isEncrypt=isMobileEncrypt;
            isSign = isMobileSign;
            keyPath = mobileKeyPath;
        }
        log.info("Is encrypted ? -> "+isEncrypt);
        log.info("Is signed ? -> "+isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        StatusResponseMessageDTO messageDTO;


        Boolean isMsgEncrypted = statusResponseDTO.getHeader().getIsMsgEncrypted();
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            Resource resource = resourceLoader.getResource(keyPath);
            InputStream fis = resource.getInputStream();

            if(isEncrypt){
                if(!isMsgEncrypted){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String responseHeaderString = objectMapper.writeValueAsString(statusResponseDTO.getHeader());
                String responseSignature = statusResponseDTO.getSignature();
                String messageString = statusResponseDTO.getMessage().toString();
                String data = responseHeaderString+messageString;
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature) , fis , p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                } catch(IOException e){
                    log.info("Rejecting the on-search request in signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }
                if(isMsgEncrypted){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(StatusResponseMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(isMsgEncrypted){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(statusResponseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, StatusResponseMessageDTO.class);
                String responseHeaderString = objectMapper.writeValueAsString(statusResponseDTO.getHeader());
                String responseSignature = statusResponseDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = responseHeaderString+messageString;
                log.info("Signature ->"+responseSignature);
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature) , fis , p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }catch(IOException e){
                    log.info("Rejecting the on-search request in signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!isMsgEncrypted){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = statusResponseDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(),"Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(StatusResponseMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(isMsgEncrypted){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(statusResponseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, StatusResponseMessageDTO.class);
            }
        }
        return messageDTO;
    }
````
41. Override validateStatusResponseDTO() method in DcValidationServiceImpl.
````
@Override
    public void validateStatusResponseDTO(StatusResponseDTO statusResponseDTO) throws IOException, G2pcValidationException {
        ObjectMapper objectMapper = new ObjectMapper();
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(statusResponseDTO.getHeader());
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerString);
        byte[] json = objectMapper.writeValueAsBytes(statusResponseDTO.getMessage());
        StatusResponseMessageDTO statusResponseMessageDTO  =  objectMapper.readValue(json, StatusResponseMessageDTO.class);
        responseHandlerService.validateStatusResponseMessage(statusResponseMessageDTO);
    }
````
42. Add below method in DcResponseHandlerService interface.
````
AcknowledgementDTO getStatusResponse(StatusResponseDTO statusResponseDTO) throws JsonProcessingException;

````
43. Override above method in DcResponseHandlerServiceImpl class.
````
@Override
    public AcknowledgementDTO getStatusResponse(StatusResponseDTO statusResponseDTO) throws JsonProcessingException {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();

        G2pcError g2pcError = txnTrackerService.updateStatusTransactionDbAndCache(statusResponseDTO);
        log.info("on-status response received from registry : {}", objectMapper.writeValueAsString(statusResponseDTO));
        log.info("on-status database updation response from sunbird - "+g2pcError.getCode());
        if (g2pcError.getCode().equals(HttpStatus.OK.toString())){
            acknowledgementDTO.setMessage(Constants.ON_STATUS_RESPONSE_RECEIVED.toString());
            acknowledgementDTO.setStatus(Constants.COMPLETED);

        } else {
            acknowledgementDTO.setMessage(Constants.INVALID_RESPONSE.toString());
            acknowledgementDTO.setStatus(Constants.PENDING);
            throw  new G2pHttpException(g2pcError);

        }
        return acknowledgementDTO;
    }
````
43. Create DcDashboardController.java for creating endpoints for dashboard of Grafana.
````
@Controller
@Slf4j
public class DcDashboardController { 
}
````
43. Add below configuration values which added in application.yml
````
  @Value("${dashboard.left_panel_url}")
    private String leftPanelUrl;

    @Value("${dashboard.right_panel_url}")
    private String rightPanelUrl;

    @Value("${dashboard.bottom_panel_url}")
    private String bottomPanelUrl;

    @Value("${dashboard.post_https_endpoint_url}")
    private String postHttpsEndpointUrl;

    @Value("${dashboard.clear_dc_db_endpoint_url}")
    private String clearDcDbEndpointUrl;

    @Value("${keycloak.dc.client.url}")
    private String dcKeyCloakUrl;

    @Value("${keycloak.dc.client.clientId}")
    private String dcClientId;

    @Value("${keycloak.dc.client.clientSecret}")
    private String dcClientSecret;

    @Value("${dashboard.left_panel_data_endpoint_url}")
    private String leftPanelDataEndpointUrl;

    @Value("${dashboard.sftp_post_endpoint_url}")
    private String sftpPostEndpointUrl;

    @Value("${dashboard.sftp_dc_data_endpoint_url}")
    private String sftpDcDataEndpointUrl;

    @Value("${dashboard.sftp_dp1_data_endpoint_url}")
    private String sftpDp1DataEndpointUrl;

    @Value("${dashboard.sftp_dp2_data_endpoint_url}")
    private String sftpDp2DataEndpointUrl;

    @Value("${dashboard.dc_status_endpoint_url}")
    private String dcStatusEndpointUrl;

    @Value("${dashboard.sftp_left_panel_url}")
    private String sftpLeftPanelUrl;

    @Value("${dashboard.sftp_right_panel_url}")
    private String sftpRightPanelUrl;

    @Value("${dashboard.sftp_bottom_panel_url}")
    private String sftpBottomPanelUrl;

    @Autowired
    private RequestBuilderService requestBuilderService;
````
44. Create below endpoint method 
````
@GetMapping("/dashboard")
    public String showDashboardPage(Model model) throws IOException, ParseException {
      String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        model.addAttribute("left_panel_url", leftPanelUrl);
        model.addAttribute("right_panel_url", rightPanelUrl);
        model.addAttribute("bottom_panel_url", bottomPanelUrl);
        model.addAttribute("post_https_endpoint_url", postHttpsEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        model.addAttribute("left_panel_data_endpoint_url", leftPanelDataEndpointUrl);
        model.addAttribute("dc_status_endpoint_url", dcStatusEndpointUrl);
        return "dashboardHttps";
    }
     @GetMapping("/dashboard/sftp")
    public String showDashboardSftpPage(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        model.addAttribute("sftp_post_endpoint_url", sftpPostEndpointUrl);
        model.addAttribute("sftp_left_panel_url", sftpLeftPanelUrl);
        model.addAttribute("sftp_right_panel_url", sftpRightPanelUrl);
        model.addAttribute("sftp_bottom_panel_url", sftpBottomPanelUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        return "dashboardSftp";
    }

    @GetMapping("/dashboard/sftp/sse")
    public String showDashboardSftpPageSse(Model model) throws IOException, ParseException {
        String jwtToken = requestBuilderService.getValidatedToken(dcKeyCloakUrl, dcClientId, dcClientSecret);
        model.addAttribute("sftp_post_endpoint_url", sftpPostEndpointUrl);
        model.addAttribute("sftp_dc_data_endpoint_url", sftpDcDataEndpointUrl);
        model.addAttribute("sftp_dp1_data_endpoint_url", sftpDp1DataEndpointUrl);
        model.addAttribute("sftp_dp2_data_endpoint_url", sftpDp2DataEndpointUrl);
        model.addAttribute("clear_dc_db_endpoint_url", clearDcDbEndpointUrl);
        model.addAttribute("jwtToken", jwtToken);
        return "dashboardSftpSse";
    }
````

45. Create method createStatusRequestSftp() for creating endpoint to listen to CSV file payload to handle using SFTP in DcController.
````
 @Operation(summary = "Listen to CSV file payload to handle using SFTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping(value = "/public/api/v1/consumer/search/sftp/csv")
    public AcknowledgementDTO createStatusRequestSftp(@RequestParam("file") MultipartFile file) {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        try {
            if (!Objects.equals(file.getContentType(), "text/csv")) {
                acknowledgementDTO.setStatus(HeaderStatusENUM.RJCT.toValue());
                acknowledgementDTO.setMessage("Invalid file type");
                return acknowledgementDTO;
            }
            String originalFilename = UUID.randomUUID() + ".csv";
            Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), originalFilename);
            Files.createFile(tempFile);
            file.transferTo(tempFile.toFile());
            SftpServerConfigDTO sftpServerConfigDTO = commonUtils.getSftpServerConfigDTO();
            Boolean status = sftpHandlerService.uploadFileToSftp(sftpServerConfigDTO, tempFile.toString(),
                    sftpServerConfigDTO.getRemoteInboundDirectory());
            Files.delete(tempFile);
            if (Boolean.FALSE.equals(status)) {
                acknowledgementDTO.setStatus(HeaderStatusENUM.RJCT.toValue());
                acknowledgementDTO.setMessage(Constants.UPLOAD_ERROR);
                return acknowledgementDTO;
            }
            acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
            acknowledgementDTO.setMessage("File uploaded successfully");
        } catch (IOException e) {
            log.error(Constants.UPLOAD_ERROR, e);
            acknowledgementDTO.setStatus(HeaderStatusENUM.RJCT.toValue());
            acknowledgementDTO.setMessage(Constants.UPLOAD_ERROR);
        }
        return acknowledgementDTO;
    }
````
47. Import below curl to run the endpoint and add payload.csv
````
curl --location 'localhost:8000/public/api/v1/consumer/search/sftp/csv' \
--form 'file=@"/home/ttpl-rt-119/Downloads/payload.csv"'
````
48. Add below 2 methods in DcController.
````
    @GetMapping("/dashboard/leftPanel/data")
    public List<HttpsLeftPanelDataDTO> fetchLeftPanelData() {
        List<HttpsLeftPanelDataDTO> leftPanelDataDTOList = new ArrayList<>();
        Optional<List<ResponseTrackerEntity>> optionalList = 
                                        responseTrackerRepository.findAllByAction("search");
        if (optionalList.isEmpty()) {
            return leftPanelDataDTOList;
        }
        List<ResponseTrackerEntity> responseTrackerEntityList = optionalList.get();
        for (ResponseTrackerEntity responseTrackerEntity : responseTrackerEntityList) {
            HttpsLeftPanelDataDTO leftPanelDataDTO = new HttpsLeftPanelDataDTO();
            leftPanelDataDTO.setMessageTs(responseTrackerEntity.getMessageTs());
            leftPanelDataDTO.setTransactionId(responseTrackerEntity.getTransactionId());
            leftPanelDataDTO.setStatus(responseTrackerEntity.getStatus());
            leftPanelDataDTOList.add(leftPanelDataDTO);
        }
        return leftPanelDataDTOList;
    }

    @GetMapping(value = "/dashboard/sftp/dc/data", produces = "text/event-stream")
    public SseEmitter sseEmitterFirstPanel() {
        return dcSftpPushUpdateService.register();
    }
````
49. Add interface DcSftpPushUpdateService in service class. 
````
package g2pc.ref.dc.client.service;

public interface DcSftpPushUpdateService {   
    SseEmitter register();

    void pushUpdate(Object update);


}
````
50. Implement DcSftpPushUpdateServiceImpl from DcSftpPushUpdateService , refer below code.
````
@Service
@Slf4j
public class DcSftpPushUpdateServiceImpl implements DcSftpPushUpdateService {
    private final List<SseEmitter> emitters = new ArrayList<>();
    public SseEmitter register() {
        int minutes = 15;
        long timeout = (long) minutes * 60000;
        SseEmitter emitter = new SseEmitter(timeout);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        log.info("SSE emitter registered" + emitter);
        return emitter;
    }
    public void pushUpdate(Object update) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(update);
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }
    public SftpDcData buildSftpDcData(String transactionId, String filename) {
        SftpDcData sftpDcData = new SftpDcData();
        sftpDcData.setMessageTs(CommonUtils.getCurrentTimeStamp());
        sftpDcData.setTransactionId(transactionId);
        sftpDcData.setFileName(filename);
        sftpDcData.setSftpDirectoryType("INBOUND");
        return sftpDcData;
    }
}
````
51. Add dto HttpsLeftPanelDataDTO in package g2pc.ref.dc.client.dto.dashboard
````
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpsLeftPanelDataDTO {

    private String messageTs;
    private String transactionId;
    private String status;
}
````
52. Add SftpDcData dto in package g2pc.ref.dc.client.dto.dashboard
````
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SftpDcData {

    private String messageTs;
    private String transactionId;
    private String fileName;
    private String sftpDirectoryType;
}
````
53. 

# 8. Keycloak configuration
### Steps for DC and DP -
1. Create data-consumer/data-provider realm. Click on Add realm shown below. 
![Alt-text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-realm-creation.png)
2. Enter appropriate name in small cases.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-add-realm.png)
3. Change token expiry time as per requirement of testing. 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-token-expiry.png)
4. Create client. 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-create-realm-button.png)
5. Add appropriate client name.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-create-client.png)
6. Select Access type is confidential , enable service account and Enable Authorization. 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-client-auth-setting.png)
7. Refer below image for client secret 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-dc-client.png)
8. Enable above all setting for admin-cli client and admin cli of master realm as well.
9. Add below scopes in client scope of admin-cli of master realm.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-master-admin-cli-client-scope.png)
10. Add below scopes in scope of admin-cli of master realm.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/keycloak-master-admin-cli-scope.png)


# SFTP configuration - 
### Below are the steps to configure SFTP - 
1. Run the below docker-compose.yml using below command.
````
sudo docker compose up
````
docker-compose.yml 
````
version: '3'
services:
  dc1_sftp:
    image: atmoz/sftp
    container_name: dc1_sftp_server
    restart: unless-stopped
    ports:
        - "2224:22"
    command: cdpi:1234:::inbound,outbound

  dp1_sftp:
    image: atmoz/sftp
    container_name: dp1_sftp_server
    restart: unless-stopped
    ports:
        - "2225:22"
    command: cdpi:1234:::inbound,outbound

  dp2_sftp:
    image: atmoz/sftp
    container_name: dp2_sftp_server
    restart: unless-stopped
    ports:
        - "2226:22"
    command: cdpi:1234:::inbound,outbound
````
2. Install any FTP client viewer. Refer below image. Add sites using below steps - 
    1. Open client and select tab shown in below image.
   ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/filezilla.png)
   2. Create site , select SFTP , add local host and port. Add username and password added in docker-compose file.
   ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/filezilla-site-create.png)
   3. Create folder structure shown below.
   ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/filezila-folder.png)
   


# Sunbird Rc Integration
### Below are the steps to install sunbird rc in local host.
1. Refer below docker-compose.yml file. Change password and host name ad per instruction given in below file. 
````
version: '2.4'

services:
  db:
    image: postgres
    container_name: {Add container name of db}
    restart: unless-stopped
    ports:
      - '{Add port}:5432'
    environment:
      - POSTGRES_DB={Add DB name}
      - POSTGRES_USER={Add user name}
      - POSTGRES_PASSWORD={Add password}
  es:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.1
    container_name: {Add container name of es}
    restart: unless-stopped
    environment:
      - discovery.type=single-node
      - 'ES_JAVA_OPTS=-Xms512m -Xmx512m'
    healthcheck:
      test: ['CMD', 'curl', '-f', 'localhost:9200/_cluster/health']
      interval: 30s
      timeout: 10s
      retries: 4
    ports:
      - '{Add port}:9200'
      - '{Add port}:9300'

  rg:
    image: dockerhub/sunbird-rc-core:v0.0.8
    container_name: {Add container name of sunbird rc}
    restart: unless-stopped
    volumes:
      - /home/{Add machine hostname}/sunbirdrc/config/schemas:/home/sunbirdrc/config/public/_schemas
    environment:
      - connectionInfo_uri=jdbc:postgresql://db:5432/registry
      - connectionInfo_username={Add username of connection of db}
      - connectionInfo_password={Add password of connection of db}
      - elastic_search_connection_url=es:{Add port like 9201}
      - search_provider=dev.sunbirdrc.registry.service.ElasticSearchService
      - search_providerName=dev.sunbirdrc.registry.service.ElasticSearchService
      - signature_enabled=false
      - authentication_enabled=false
    ports:
      - '{Add port to run registry like 8080,8081}:8081'
    depends_on:
      - db
      - es
````
2. Create folder structure mentioned below. 
````
/home/{Add machine hostname}/sunbirdrc/config/schemas
````
3. 
4. Open the terminal in the folder where docker-compose.yml is there.
5. Recreate and start the Elasticsearch container:
````
sudo docker-compose up -d --no-deps --force-recreate es
````
This command rebuilds and recreates the Elasticsearch container (g2pc-es-1), excluding its dependencies.
It ensures that the container starts with a fresh configuration.
6. create and start the database container:
````
sudo docker-compose up -d --no-deps --force-recreate db
````
This command rebuilds and recreates the database container (g2pc-db-1), excluding its dependencies.
It ensures that the container starts with a fresh configuration.
7. Recreate and start the Sunbird-RC registry container:
````
sudo docker-compose up -d --no-deps --force-recreate rg
````
This command rebuilds and recreates the Sunbird-RC registry container (g2pc-rg-1), excluding its dependencies.
It ensures that the container starts with a fresh configuration.
8. Check the running containers:
````
sudo docker ps
````
Verify that the containers are up and running.
9. Adjust paths and container names accordingly based on your specific setup and configurations. 
10. These commands use Docker Compose to manage and orchestrate the containers. 
11. The --no-deps flag ensures that only the specified service is recreated without starting its dependencies. 
12. The --force-recreate flag ensures the recreation of the container even if it is already running.
13. Use below command to restart the registry.
````
 sudo docker restart {container-name}
````
14. 
  



























