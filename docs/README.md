# Overview of G2p connect - Registry
- Governments give money to people for various reasons like subsidies, pensions, scholarships, and emergency help. People can choose how they want to receive this money, like in cash, through their bank, on their phone, or with vouchers.

- But, each government department has to set up its own system to check if people are eligible for the money, make sure transactions are real, and actually send the money. They have to talk to different departments to gather all the needed information, and this leads to a lot of duplicate work and problems.
- Simply putting all the data in one place doesn't work well because it can be a security risk and needs a lot of new systems. The usual approach of just putting things 'online' doesn't consider the real issues like politics, how people use technology, and the need for new ideas.
- A G2P DPI - Registry , is about creating a system that works together and respects privacy, security, and individual choices. The usual steps for giving money involve checking if people qualify, confirming their identity, and sending the money to their chosen method.
- To make this process better, G2P Connect suggests building a secure, decentralized system that different departments can customize. This way, they can share common elements, solve problems, and make the process more efficient.
- G2P Connect is an open-source project that helps different government agencies in a country work together to deliver digital payments from start to finish.
- The G2P transaction process involves an individual asking for money, providing their ID, and going through some security steps. The system checks if they qualify by looking at different government databases.

## G2p specifications
- G2P Connect API Specifications is a project that makes it easy for different systems to work together. It sets rules for how they should talk to each other
- The main goals of G2P Connect Specifications are to make sure systems can work seamlessly together and follow the rules set by the country. It also aims to be flexible, meaning it can adapt to existing standards and use common methods like OAuth2 for security.
- The message structure used in G2P Connect is like a package with a signature and a header. The header includes important information like the version, message ID, and what action is being taken.
- The specifications also allow easy integration of different data types and ensure secure communication through digital signatures and encryption.
- The focus is on standardizing core interfaces, acting as connectors between solutions and enabling countries to implement a variety of use cases.
- G2P Connect doesn't care how systems send messages; it can work with different methods like HTTPS, messaging events, or file exchanges. It also makes sure that the dates, times, and currency codes used in the messages are in a format that everyone can understand.
- In simple terms, G2P Connect API Specifications is like a rulebook that different systems follow to work together when giving money to people. It's flexible, secure, and makes sure everyone understands each other.


# Overview / List of libraries
### 1. G2pc-core-lib - 
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


### 2. G2pc-dp-core-lib
- The G2pc-dp-core-lib defines entities and services specific to data providers. 
- It incorporates methods for constructing responses and managing requests within custom data provider services. 
- This library encapsulates both an entity and repositories responsible for handling the data stored in tables dedicated to message tracking and transaction information. 
- Also , service implementation manages the tracking of transactions, including saving request details, determining record counts, constructing search responses, and updating transaction statuses. 
- It also includes building cache requests, validating request headers and messages against JSON schemas, and handling transaction tracking in both Redis and a database. 
- It integrates with other services and provides detailed error handling and logging. Also constructing response DTOs, managing encryption and signatures, sending responses via HTTP, and handling tokens.

### 3. G2pc-dc-core-lib
- The G2pc-dc-core-lib defines entities and services specific to data consumers. 
- It incorporates methods for constructing requests and managing responses within custom data consumer services.
- This library defines functionality for building and sending requests in a secure manner, involving encryption, digital signatures, token management, and caching.
- Also provides functionality for updating a cache, validating response headers, and validating response messages against predefined JSON schemas.
- This service is basically to handle and process responses within a system.
- Below is the communication diagram of DP and DC -

  ![Alt text]( https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/DC-DP-communication.png "a title")

# Data Provider (DP) Implementation
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
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/search_sequence_diagram.png)
- Implementation explained in below point when it act like data consumer -
  1. When it acts like a consumer , it needs to define a scheduler. Scheduler is nothing but a framework that allows you to schedule and execute tasks at specific intervals or times. 
  2. In this scheduler , dp will check whether there is any data stored in pending status with a particular cache key corresponding to that data provider. 
  3. If it gets data it will build the response data and the call /on-search endpoint is defined in the data consumer . Please refer to Image 2 for the same. 
  4. Refer below for understanding of flow from dp to parent libraries. 
 ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dp-scheduler-sequence-dia.png)

# How to create a Data Provider ?
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
<dependency>+
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
````
4. Create package structure shown below.                                                                                         
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-package-strcuture.png)
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
9. Add below controller class for dashboard purpose.
````
@Controller
public class DpDashboardController {
````
10. In the service package , create the respective DP ResponseBuilderService.java interface. Refer below example.
````
public interface FarmerResponseBuilderService {
   RegRecordFarmerDTO getRegRecordFarmerDTO(FarmerInfoEntity farmerInfoEntity);
   }
````
10. In the service package , create the respective ValidationService.java interface. Refer below example.
````
public interface FarmerValidationService {
   void validateRequestDTO (RequestDTO requestDTO) throws Exception;
   void validateQueryDto (QueryDTO queryFarmerDTO) throws G2pcValidationException, JsonProcessingException;
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
12. Create DpCommonUtils for handling token. 
````
import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DpCommonUtils {
    }
}
````
13. Add below autowired dependent bean and values configured with application.yml to authenticate user.
````
    @Value("${keycloak.dp.client.realm}")
    private String keycloakRealm;

    @Value("${keycloak.dp.master.getClientUrl}")
    private String getClientUrl;

    @Value("${crypto.to_dc.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.to_dc.support_signature}")
    private boolean isSign;

    @Value("${keycloak.dp.client.url}")
    private String keycloakURL;

    @Value("${keycloak.dp.client.clientId}")
    private String keycloakClientId;

    @Value("${keycloak.dp.client.clientSecret}")
    private String keycloakClientSecret;

    @Value("${keycloak.dp.master.url}")
    private String masterUrl;

    @Value("${keycloak.dp.master.clientId}")
    private String masterClientId;

    @Value("${keycloak.dp.master.clientSecret}")
    private String masterClientSecret;

    @Value("${keycloak.dp.username}")
    private String adminUsername;

    @Value("${keycloak.dp.password}")
    private String adminPassword;

    @Autowired
    G2pTokenService g2pTokenService;
````
14.  Create handleToken() method.
````
 public void handleToken() throws G2pHttpException, JsonProcessingException {

````
15. Add below code to introspect the token whether it is from valid keycloak dp or not in handleToken() method.
````
  log.info("Is encrypted ? -> " + isEncrypt);
        log.info("Is signed ? -> " + isSign);
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspectUrl = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspectUrl, token,
                keycloakClientId, keycloakClientSecret);
        log.info("Introspect response -> " + introspectResponse.getStatusCode());
        log.info("Introspect response body -> " + introspectResponse.getBody());
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
````
16. Add below code in handleToken() method for validateToken using g2pc-core predefined methods.
````
 if (!g2pTokenService.validateToken(masterUrl, getClientUrl,
                g2pTokenService.decodeToken(token), masterClientId, masterClientSecret,
                adminUsername, adminPassword)) {
            //TODO:check this -> done
            throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
````
17. 
13. Add below autowired dependencies in the RegistryController class.
````
    @Autowired
    private RequestHandlerService requestHandlerService;
    
    @Autowired
    FarmerValidationService farmerValidationService;
    
    @Autowired
    private DpCommonUtils dpCommonUtils;
    
    @Autowired
    private MsgTrackerRepository msgTrackerRepository;
````
13. Add below application.yml and update as per below instructions.
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
    url: jdbc:postgresql://g2pc-spec-demo-rds.cs9zoco3zxkq.ap-south-1.rds.amazonaws.com:5432/dp1?currentSchema=g2pc
    username: postgres
    password: K6tnrCU0wqXOwPW

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
  port: 9001
  error:
    include-message: always

spring.data.redis:
  repositories.enabled: false
  #  host: 3.109.26.38
  #  password: cdpi@99221
  #  port: 6379
  host: localhost
  password: 123456789
  port: 6376

client:
  api_urls:
    client_search_api: "http://localhost:8000/private/api/v1/registry/on-search"

keycloak:
  from_dc:
    url: "https://g2pc-dc-lab.cdpi.dev/auth/realms/data-consumer/protocol/openid-connect/token"
    clientId: dc-client
    clientSecret: co0rJfm3mIq0OXysAt6DtDjibOHkcktY
  dp:
    url: https://g2pc-dp1-lab.cdpi.dev/auth
    username: admin
    password: cdpi@9923
    master:
      url: https://g2pc-dp1-lab.cdpi.dev/auth/realms/master/protocol/openid-connect/token
      getClientUrl: https://g2pc-dp1-lab.cdpi.dev/auth/admin/realms/dp-farmer/clients
      clientId: admin-cli
      clientSecret: G7rVA27HI5UpzMJfomRvaQHubtbAcWcN
    client:
      url: https://g2pc-dp1-lab.cdpi.dev/auth/realms/dp-farmer/protocol/openid-connect/token
      realm: dp-farmer
      clientId: dp-farmer-client
      clientSecret: 55VuMuin1T8xbYSUu5zAJAebA05tSwkX

crypto:
  to_dc:
    support_encryption: true
    support_signature: true
    password: "farmer_on_search"
    key_path: "classpath:farmer_on_search.p12"
    id: FARMER
  from_dc:
    support_encryption: true
    support_signature: true
    password: "farmer_search"
    key_path: "classpath:farmer_search.p12"

dashboard:
  dp_dashboard_url: "http://3.109.26.38:3005/d-solo/e62ae08b-a6e1-4095-af79-c36f02b8fae2/dp1-dashboard?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
````
14. Add below attributes as per your requirement -  
    1. Change db name (gtwop) , schema name (farmer) , username and password for db connection as per your postgres/mysql connection. 
    2. client.api_urls.client_search_api -> change port as  respective on-search api. 
    3. keycloak.from-dc.url -> create realm for data consumer and replace name with keycloak data-consumer realm name. 
    4. keycloak.from-dc.client-id -> respective client id of created realm. 
    5. keycloak.from-dc.client.secret -> respective client secret of created realm.
    6. keycloak.dp.url -> keycloak dp url
    7. keycloak.dp.username -> dp keycloak admin username 
    8. keycloak.dp.password -> dp keycloak admin password
    9. keycloak.dp.master.url -> master token url for particular dp keycloak , change host name in case of change 
    10. keycloak.dp.master.getClientUrl -> client details api , replace host and realm name in case. 
    11. keycloak.dp.master.clientId -> client id of admin client of master realm
    12. keycloak.dp.master.clientSecret -> client secret of admin client of master realm
    13. keycloak.dp.client.url -> realm token url , replace realm id
    14. keycloak.dp.realm -> realm name of dp
    15. keycloak.dp.clientId -> client Id of client created in dp realm
    16. keycloak.dp.clientSecret -> client secret of client created in dp realm
    17. crypto.to_dc.support_encryption -> flag of encryption when scheduler will call /on-search api
    17. crypto.to_dc.support_signature -> flag of signature when scheduler will call /on-search api
    18. crypto.to_dc.password -> password of on_search.p12 password
    19. crypto.to_dc.key.path -> path of on_search.p12 file
    20. crypto.to_dc.id -> flag of Farmer id for data consumer.
    21. crypto.from_dc.support_encryption ->flag of encryption when the data consumer will call /search to validate the configuration.
    22. crypto.from_dc.support_signature ->  flag of signature when the data consumer will call /search to validate the configuration.
    23. crypto.from_dc.password -> password of search.p12 password
    24. crypto.from_dc.key.path -> path of search.p12 file
    25. Dashboard.dp_dashboard_url ->

    
16. Define below endpoint in RegistryController.
````
@Operation(summary = "Receive search request")
@ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
       @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
       @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
       @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
@PostMapping("/private/api/v1/registry/search")
public AcknowledgementDTO registerCandidateInformation(@RequestBody String requestString) throws Exception {

````


18. Add below code in the same method to add subtype in objectMapper to convert String in requestDTO.
````
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerSubtypes(RequestHeaderDTO.class,
       ResponseHeaderDTO.class, HeaderDTO.class);


RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
       readValue(requestString);
RequestMessageDTO messageDTO = null;
````
19. Add below code snippet to validate signature and encryption.
````
Map <String , Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
requestDTO.setMessage(messageDTO);
````
20. Add below code snippet to validate requestDTO as per g2p specifications and  build cache request for Request string. In this buildCacheRequest it has already been defined in parent libraries , just need to call.
````
String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
try {
   farmerValidationService.validateRequestDTO(requestDTO);
   return requestHandlerService.buildCacheRequest(
           objectMapper.writeValueAsString(requestDTO), cacheKey);
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
21. Add below 2 methods Custom Exception handling using spring boot annotations in RegistryController.
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
22. Create below endpoint for clearing the db.
````
  @GetMapping("/public/api/v1/registry/clear-db")
    public void clearDb() throws G2pHttpException, IOException {
        //dpCommonUtils.handleToken();
        msgTrackerRepository.deleteAll();
        log.info("DP-1 DB cleared");
    }
````
22. Create Query and Query param dto for data provider requirement in dto.request package. Below are examples.
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
23. Create regRecordDTO for data provider as per on search endpoint requirement in dto.response package shown below.
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
24. To get data provider information create a data-provider info table in db , entity and repository as shown below.
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
25. Define below method in ValidationServiceImpl as it has implemented from ValidationService interface.
````
@Override
public RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO) throws Exception {
````
26. Define below autowired beans and configurations in ValidationServiceImpl.
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
27. Add below code snippet in signatureValidation method. This is the validation for signature and encryption to check whether this is transferred correctly.
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
28. Create a schema folder in the resource folder for respective data provider Query.
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dp_schema.png)
29. With reference to below Query of farmer data provider. Refer specification -
    [specification](https://g2p-connect.github.io/specs/release/html/registry_core_api_v1.0.0.html#tag/Async/operation/post_reg_search)
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dp-specs-json.png)
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/dp-specs.png)
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
              "type": "string",
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
30. Implement method from ValidationService and change schema name in path.
````
@Override
public void validateQueryDto(QueryDTO queryFarmerDTO) throws JsonProcessingException, G2pcValidationException {

ObjectMapper objectMapper = new ObjectMapper();
log.info("Query object -> " + queryFarmerDTO);
String queryString = new ObjectMapper()
       .writerWithDefaultPrettyPrinter()
       .writeValueAsString(queryFarmerDTO);
log.info("Query String" + queryString);
InputStream schemaStreamQuery = FarmerValidationServiceImpl.class.getClassLoader()
       .getResourceAsStream("schema/farmerQuerySchema.json");
JsonNode jsonNode = objectMapper.readTree(queryString);
JsonSchema schema = null;
if (schemaStreamQuery != null) {
   schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
           getSchema(schemaStreamQuery);
}
Set<ValidationMessage> errorMessage = schema.validate(jsonNode);
List<G2pcError> errorcombinedMessage = new ArrayList<>();
for (ValidationMessage error : errorMessage) {
   log.info("Validation errors" + error);
   errorcombinedMessage.add(new G2pcError("", error.getMessage()));
}
if (errorMessage.size() > 0) {
   throw new G2pcValidationException(errorcombinedMessage);
   }

````
31. Implement below method from ValidationService in ValidationServiceImpl.
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
List<SearchRequestDTO> searchRequestList = messageDTO.getSearchRequest();
for(SearchRequestDTO searchRequestDTO : searchRequestList){
   String queryString = objectMapper.writeValueAsString(searchRequestDTO.getSearchCriteria().getQuery());
   QueryDTO queryFarmerDTO = objectMapper.readerFor(QueryDTO.class).
           readValue(queryString);
   validateQueryDto(queryFarmerDTO);
}
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
    @Value("${client.api_urls.client_search_api}")
    String onSearchURL;
    
    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    private ResponseBuilderService responseBuilderService;

    @Autowired
    private FarmerResponseBuilderService farmerResponseBuilderService;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Autowired
    private MsgTrackerRepository msgTrackerRepository;

    @Autowired
    private TxnTrackerRepository txnTrackerRepository;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;

    @Autowired
    private ResourceLoader resourceLoader;
    
    @Value("${keycloak.from-dc.client-id}")
    private String dcClientId;

    @Value("${keycloak.from-dc.client-secret}")
    private String dcClientSecret;

    @Value("${keycloak.from-dc.url}")
    private String keyClockClientTokenUrl;

    @Value("${crypto.to_dc.id}")
    private String dp_id;

    @Value("${crypto.to_dc.key.path}")
    private String farmer_key_path;
````
32. Define below method in Scheduler.
````
@Scheduled(cron = "0 */1 * ? * *")// runs every 1 min.
@Transactional
public void responseScheduler() throws IOException {
````  
33. Define try catch in the same method.
34. Add below snippet in method.
````
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
````
35.  Call method from txnTrackerRedisService to get cache list.
````
List<String> cacheKeysList = txnTrackerRedisService.getCacheKeys(Constants.CACHE_KEY_SEARCH_STRING);
````
36. Check whether in list the status in PNDG or not
````
if (cacheDTO.getStatus().equals(HeaderStatusENUM.PDNG.toValue())) {
````
37. Add below code snippet to in FarmerResponseBuilderServiceImpl
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
38. Add below snippet in scheduler class if condition
````
  {
                    RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());
                    RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
                    String transactionId = messageDTO.getTransactionId();

                    MsgTrackerEntity msgTrackerEntity = txnTrackerDbService.saveRequestDetails(requestDTO);
                    List<QueryDTO> queryDTOList = msgTrackerEntity.getTxnTrackerEntityList().stream()
                            .map(txnTrackerEntity -> {
                                try {
                                    return objectMapper.readValue(txnTrackerEntity.getQuery(), QueryDTO.class);
                                } catch (JsonProcessingException e) {
                                    return null;
                                }
                            }).toList();
                    List<String> refRecordsStringsList = farmerResponseBuilderService.getRegFarmerRecords(queryDTOList);

                    List<SearchResponseDTO> searchResponseDTOList = txnTrackerDbService.getUpdatedSearchResponseList(
                            requestDTO, refRecordsStringsList);

                    ResponseHeaderDTO headerDTO = responseBuilderService.getResponseHeaderDTO(msgTrackerEntity);

                    ResponseMessageDTO responseMessageDTO = responseBuilderService.buildResponseMessage(transactionId, searchResponseDTOList);
                    Map<String, Object> meta = (Map<String, Object>) headerDTO.getMeta().getData();
                    meta.put(CoreConstants.DP_ID, dp_id);
                    requestDTO.getHeader().getMeta().setData(meta);
                    String responseString = responseBuilderService.buildResponseString("signature",
                            headerDTO, responseMessageDTO);
                    responseString = CommonUtils.formatString(responseString);
                    log.info("on-search response = {}", responseString);
                    txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    Resource resource = resourceLoader.getResource(farmer_key_path);
                    String encryptedSalt = "";
                    InputStream fis = resource.getInputStream();
                    G2pcError g2pcError = responseBuilderService.sendOnSearchResponse(responseString, onSearchURL, dcClientId, dcClientSecret, keyClockClientTokenUrl, fis, encryptedSalt);
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

# Data Consumer (DC) Implementation
- In DC implementation , as explained in Overview of libraries , dependency of G2pc-dc-core-lib needs to be added.
- Data consumers are  going to act as consumers as well as providers.
Implementation explained in below point when it act like data consumer -
  1. When it acts like a consumer , it needs to define an endpoint which accepts payload . Example Shown in the image below. 
![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/patload_postman.png)
  2. Using this data consumer will decide which data provider’s endpoint it needs to call and which request it needs to build.
  3. Once a request is created /search endpoint it will call and once positive acknowledgement is there it will save pending status in cache for particular transaction id. Refer below for more understanding.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/payload_sequence_diagram.png)
- Implementation explained in below point when it act like data provider -
  1. As shown in Figure 2 data provider needs to implement the end point and also make calls to the endpoint of the data provider. 
  2. At first Data consumer service needs to write the /on-search end-point. 
  3. This endpoint will receive a responseString transferred by the data provider. 
  4. In this endpoint authentication also needs to be defined to ensure that the correct user is accessing the endpoint or not. 
  5. Also need to make sure that the correct signature and valid message is received. 
  6. This responseString will get validated as per g2p specification. Please refer to the link mentioned and image below.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/on_search_spec.png)
  7. Once responseString gets validated data consumers should update that data in redis cache and send acknowledgement back to the data consumer. Refer below for more understanding.
  ![Alt text](https://github.com/G2P-Connect/g2pc-registry/blob/alpha-1.0/docs/src/images/on_search_seqeunce_dia.png)

# How to create a Data Consumer ? 
1. Create a spring boot application with the latest spring-boot version , maven and Java 17. And Click on generate to download.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/spring_boot_dc_creation.png)
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
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-csv</artifactId>
            <version>1.10.0</version>
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
````
4. Create package structure shown below.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dc-package-structure.png)
5. Add .p12 files for search received from dp and on-search 
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/.p12-dc.png)
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
    url: jdbc:postgresql://g2pc-spec-demo-rds.cs9zoco3zxkq.ap-south-1.rds.amazonaws.com:5432/dc1?currentSchema=g2pc
    username: postgres
    password: K6tnrCU0wqXOwPW

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
  port: 8000
  error:
    include-message: always

spring.data.redis:
  repositories.enabled: false
  host: 3.109.26.38
  password: cdpi@99221
  port: 6379


keycloak:
  from_dp:
    farmer:
      url: "https://g2pc-dp1-lab.cdpi.dev/auth/realms/dp-farmer/protocol/openid-connect/token"
      clientId: "dp-farmer-client"
      clientSecret: "55VuMuin1T8xbYSUu5zAJAebA05tSwkX"
    mobile:
      url: "https://g2pc-dp2-lab.cdpi.dev/auth/realms/dp-mobile/protocol/openid-connect/token"
      clientId: "dp-mobile-client"
      clientSecret: "d9yPYp8G2nYLh1ztdeqvdvtxEYqx63Xg"
  dc:
    url: https://g2pc-dc-lab.cdpi.dev/auth
    username: admin
    password: cdpi@9922
    master:
      url: https://g2pc-dc-lab.cdpi.dev/auth/realms/master/protocol/openid-connect/token
      getClientUrl: https://g2pc-dc-lab.cdpi.dev/auth/admin/realms/data-consumer/clients
      clientId: admin-cli
      clientSecret: bCfUQy4z4NKiiz82zScJdKGtbKbchkhs
    client:
      url: https://g2pc-dc-lab.cdpi.dev/auth/realms/data-consumer/protocol/openid-connect/token
      realm: data-consumer
      clientId: dc-client
      clientSecret: co0rJfm3mIq0OXysAt6DtDjibOHkcktY

crypto:
  to_dp_farmer:
    support_encryption: true
    support_signature: true
    password: "farmer_search"
    key_path: "classpath:farmer_search.p12"
  to_dp_mobile:
    support_encryption: true
    support_signature: true
    password: "mobile_search"
    key_path: "classpath:mobile_search.p12"
  from_dp_farmer:
    support_encryption: true
    support_signature: true
    password: "farmer_on_search"
    key_path: "classpath:farmer_on_search.p12"
    id: FARMER
  from_dp_mobile:
    support_encryption: true
    support_signature: true
    password: "mobile_on_search"
    key_path: "classpath:mobile_on_search.p12"
    id: MOBILE

registry:
  api_urls:
    farmer_search_api: "http://localhost:9001/private/api/v1/registry/search"
    mobile_search_api: "http://localhost:9002/private/api/v1/registry/search"

dashboard:
  left_panel_url: "http://3.109.26.38:3005/d-solo/cb26f39f-97f3-43ea-9f42-68d49d9822a3/left-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  right_panel_url: "http://3.109.26.38:3005/d-solo/d9f9c625-934b-4a65-995f-c742daad6387/right-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  bottom_panel_url: "http://3.109.26.38:3005/d-solo/a25a6c65-fda7-4fdd-80a7-80442aed17e8/bottom-panel-data?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
  post_endpoint_url: "https://g2pc-dc-lab.cdpi.dev/dc-client/public/api/v1/consumer/search/csv"
  clear_dc_db_endpoint_url: "http://localhost:8000/private/api/v1/registry/clear-db"
  clear_dp1_db_endpoint_url: "http://localhost:9001/private/api/v1/registry/clear-db"
  clear_dp2_db_endpoint_url: "http://localhost:9002/private/api/v1/registry/clear-db"
```` 

7. Add below attributes as per your requirement -
   1. Change db name (gtwop) , schema name (farmer) , username and password for db connection as per your postgres/mysql connection. 
   2. spring.data.redis.host -> add redis host 
   3. spring.data.redis.password -> password of dp redis port
   4. spring.data.redis.port -> port assigned for redis of particular dp
   5. keycloak.from_dp.{dp-name}.url -> url of token creation of particular dp , replace realm name with respective dp.
   6. keycloak.from_dp.{dp-name}.clientId -> client id of particular dp client , check in below image.
   7. keycloak.from_dp.{dp-name}.clientSecret -> client secret of particular dp client , check in below image.
   ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-dp-client-secret.png)
   8. keycloak.dc.url -> hosting url of dc 
   9. keycloak.dc.username -> authentication username of hosting url of dc
   10. keycloak.dc.password -> authentication password of hosting url of dc
   11. keycloak.dc.master.url -> token endpoint url of master realm of data-consumer.
   12. keycloak.dc.master.getClientUrl -> get client by realm id endpoint of particular dc. replace host and realm id for particular data consumer
   13. keycloak.dc.master.clientId -> admin cli client id of master realm of dc
   14. keycloak.dc.master.clientSecret -> admin cli client secret of master realm of dc
   15. keycloak.dc.client.url -> token endpoint url of data-consumer realm of data-consumer.
   16. keycloak.dc.client.realm -> dc realm name
   17. keycloak.dc.client.clientId -> dc client id 
   18. keycloak.dc.client.clientSecret -> dc client secret 
   ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-dc-client.png)
   19. crypto.to_dp_{dp-name}.support_encryption -> encryption flag of particular dp of search endpoint
   20. crypto.to_dp_{dp-name}.support_signature -> signature flag of particular dp of search endpoint
   21. crypto.to_dp_{dp-name}.password -> password of .p12 file particular dp of search endpoint
   22. crypto.to_dp_{dp-name}.key_path -> key path of .p12 file particular dp of search endpoint
   23. crypto.from_dp_{dp-name}.support_encryption -> encryption flag of particular dp of on-search endpoint 
   24. crypto.from_dp_{dp-name}.support_signature -> signature flag of particular dp of on-search endpoint 
   25. crypto.from_dp_{dp-name}.password -> password of .p12 file particular dp of on-search endpoint 
   26. crypto.from_dp_{dp-name}.key_path -> key path of .p12 file particular dp of on-search endpoint
   27. crypto.from_dp_{dp-name}.id -> id flag for dp
   28. registry.api_urls.{dp-name}_search_api -> search end point of dp
   29. dashboard.clear_dc_db_endpoint_url -> clear db endpoint of dc
   30. dashboard.clear_dp_db_endpoint_url -> clear db endpoints of multiple dps
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
    
    @Value("${dashboard.clear_dp1_db_endpoint_url}")
    private String farmerClearDbURL;
    
    @Value("${keycloak.from_dp.farmer.clientId}")
    private String farmerClientId;

    @Value("${keycloak.from_dp.farmer.clientSecret}")
    private String farmerClientSecret;
    
    @Value("${keycloak.from_dp.farmer.url}")
    private String keycloakFarmerTokenUrl;
    
    @Value("${crypto.to_dp_farmer.support_encryption}")
    private boolean isFarmerEncrypt;

    @Value("${crypto.to_dp_farmer.support_signature}")
    private boolean isFarmerSign;
    
    @Value("${crypto.to_dp_farmer.key_path}")
    private String farmerKeyPath;

    @Value("${crypto.to_dp_farmer.password}")
    private String farmerKeyPassword;

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
        farmerRegistryMap.put(CoreConstants.KEY_PATH, farmerKeyPath);
        farmerRegistryMap.put(CoreConstants.KEY_PASSWORD, farmerKeyPassword);
        farmerRegistryMap.put(CoreConstants.DP_SEARCH_URL, farmerSearchURL);
        farmerRegistryMap.put(CoreConstants.DP_CLEAR_DB_URL, farmerClearDbURL);
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
13. Create below endpoint for triggering dc communication using only one data.
````
    @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
    @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
    @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
    @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/public/api/v1/consumer/search/payload")
    public AcknowledgementDTO createSearchRequestsFromPayload(@RequestBody Map<String, Object> payloadMap) throws Exception {
````
14. Add below code snippet to request from payload
````
    log.info("Payload received from postman");
    AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
    if (ObjectUtils.isNotEmpty(payloadMap)) {
    acknowledgementDTO = dcRequestBuilderService.generateRequest(Collections.singletonList(payloadMap));
    }
    return acknowledgementDTO;
````
15. To run above endpoint refer below curl.
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
16. Create below end point for triggering dc communication for multiple data using csv file
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
            acknowledgementDTO = dcRequestBuilderService.generatePayloadFromCsv(payloadFile);
        }
        return acknowledgementDTO;
    }
````
17. To run above endpoint refer below curl and create one payload.csv with multiple data.
````
curl --location 'localhost:8000/private/api/v1/consumer/search/csv' \
--form 'file=@"/home/ttpl-rt-119/Downloads/payload.csv"'
````
18. Add below exception handling in the DC controller.
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

    AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList) throws Exception;

    AcknowledgementDTO generatePayloadFromCsv(MultipartFile payloadFile) throws Exception;
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
````
23. Override below method generateRequest() from interface.
````
 public AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList) throws Exception {
        Map<String, G2pcError> g2pcErrorMap = new HashMap<>();

        List<Map<String, Object>> queryMapList = requestBuilderService.createQueryMap(payloadMapList, registryConfig.getQueryParamsConfig().entrySet());
        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig().entrySet()) {
            List<Map<String, Object>> queryMapFilteredList = queryMapList.stream()
                    .map(map -> map.entrySet().stream()
                            .filter(entry -> entry.getKey().equals(configEntryMap.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).toList();

            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig().get(configEntryMap.getKey());
            List<SearchCriteriaDTO> searchCriteriaDTOList = new ArrayList<>();
            for (Map<String, Object> queryParamsMap : queryMapFilteredList) {
                SearchCriteriaDTO searchCriteriaDTO = requestBuilderService.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap);
                searchCriteriaDTOList.add(searchCriteriaDTO);
            }
            String transactionId = CommonUtils.generateUniqueId("T");
            String requestString = requestBuilderService.buildRequest(searchCriteriaDTOList, transactionId);
            try {
                Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                String encryptedSalt = "";
                InputStream fis = resource.getInputStream();
                G2pcError g2pcError = requestBuilderService.sendRequest(requestString,
                        registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                        fis, encryptedSalt,
                        registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString());
                g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);
                log.info("DP_SEARCH_URL = {}", registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString());

                txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue());
                txnTrackerService.saveRequestTransaction(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), transactionId);
                txnTrackerService.saveRequestInDB(requestString, registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
            } catch (Exception e) {
                log.error("Exception in generateRequest : ", e);
            }
        }
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(g2pcErrorMap);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        return acknowledgementDTO;
    }
````
24. To generate request from csv file overwrite below method and one private method to iterate csv file.
````
  @Override
    public AcknowledgementDTO generatePayloadFromCsv(MultipartFile payloadFile) throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(payloadFile.getInputStream()));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        List<Map<String, Object>> payloadMapList = getPayloadMapList(csvParser);
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        if (ObjectUtils.isNotEmpty(payloadMapList)) {
            acknowledgementDTO = generateRequest(payloadMapList);
        }
        return acknowledgementDTO;
    }

    private static List<Map<String, Object>> getPayloadMapList(CSVParser csvParser) {
        List<CSVRecord> csvRecordList = csvParser.getRecords();
        CSVRecord headerRecord = csvRecordList.get(0);
        List<String> headerList = new ArrayList<>();
        for (int i = 0; i < headerRecord.size(); i++) {
            headerList.add(headerRecord.get(i));
        }
        List<Map<String, Object>> payloadMapList = new ArrayList<>();
        for (int i = 1; i < csvRecordList.size(); i++) {
            CSVRecord csvRecord = csvRecordList.get(i);
            Map<String, Object> payloadMap = new HashMap<>();
            for (int j = 0; j < headerRecord.size(); j++) {
                payloadMap.put(headerList.get(j), csvRecord.get(j));
            }
            payloadMapList.add(payloadMap);
        }
        return payloadMapList;
    }
````
25. Create on-search endpoint , refer below snippet , there are methods called in this methods refer code after this point.
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

    

````
27. Add below method in DcCommonUtils.java to handle and validate token.
````
    public void handleToken() throws G2pHttpException, JsonProcessingException {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, dcClientId, dcClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterUrl, getClientUrl, g2pTokenService.decodeToken(token), masterClientId, masterClientSecret, adminUsername, adminPassword)) {
            
            throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
        }
    }
````
28. Create DcValidationService interface with 3 methods , validateRegRecords() this method is dc specific , to validate query params.
````
@Service
public interface DcValidationService {

    public void validateResponseDto(ResponseDTO responseDTO) throws Exception;

    public void validateRegRecords(ResponseMessageDTO messageDTO) throws G2pcValidationException, IOException;

    ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception;
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
32. Override validateResponse() and validateRegRecord() method , in this for another dp validateRegRecord Method will be different as per query params.
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
        validateRegRecords(messageDTO);
        responseHandlerService.validateResponseMessage(messageDTO);
    }

    /**
     * Validate reg records.
     *
     * @param messageDTO the message dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public void validateRegRecords(ResponseMessageDTO messageDTO) throws G2pcValidationException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<SearchResponseDTO> searchResponseList = messageDTO.getSearchResponse();
        for(SearchResponseDTO searchResponseDTO : searchResponseList){
            DataDTO dataDTO = searchResponseDTO.getData();
            String regRecordString = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(dataDTO.getRegRecords());
            log.info("regRecordString -> " + regRecordString);
            if(!regRecordString.equals("null")){
                InputStream schemaStream;
                if(dataDTO.getRegType().toString().equals("ns:MOBILE_REGISTRY")){
                    schemaStream  = DcValidationServiceImpl.class.getClassLoader()
                            .getResourceAsStream("schema/RegRecordMobileSchema.json");
                } else {
                    schemaStream = DcValidationServiceImpl.class.getClassLoader()
                            .getResourceAsStream("schema/RegRecordFarmerSchema.json");
                }
                JsonNode jsonNodeMessage = objectMapper.readTree(regRecordString);
                JsonSchema schemaRegRecord = null;
                if (schemaStream != null) {
                    schemaRegRecord = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                            getSchema(schemaStream);
                }
                Set<ValidationMessage> errorMessage = schemaRegRecord.validate(jsonNodeMessage);
                List<G2pcError> errorCombinedMessage = new ArrayList<>();
                for (ValidationMessage error : errorMessage) {
                    log.info("Validation errors in Reg records" + error);
                    errorCombinedMessage.add(new G2pcError("", error.getMessage()));
                }
                if (errorMessage.size() > 0) {
                    throw new G2pcValidationException(errorCombinedMessage);
                }
            }
        }

    }
````
33. Add schema to validate regRecord / respective query params , refer below schema
````
{
  "$schema": "https://json-schema.org/draft-04/schema#",
  "$id": "https://example.com/message.schema.json",
  "title": "Message schema",
  "description": "",
  "additionalProperties": false,
  "type": "object",
  "properties": {
    "farmer_id": {
      "type": "string",
      "$ref": "#/definitions/nonEmptyString"
    },
    "farmer_name": {
      "type": "string",
      "$ref": "#/definitions/nonEmptyString"
    },
    "season": {
      "type": "string",
      "$ref": "#/definitions/nonEmptyString"
    },
    "payment_status": {
      "type": "string",
      "$ref": "#/definitions/nonEmptyString"
    },
    "payment_date": {
      "type": "string",
      "$ref": "#/definitions/nonEmptyString"
    },
    "payment_amount": {
      "type": "number"
    }
  }  ,
  "definitions": {
    "nonEmptyString": {
      "type": "string",
      "minLength": 1
    }
  }
}
````
34. Create DcResponseHandlerService interface to handle the response
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

        txnTrackerService.updateTransactionDbAndCache(responseDTO);
        log.info("on-search response received from registry : {}", objectMapper.writeValueAsString(responseDTO));
        acknowledgementDTO.setMessage(Constants.ON_SEARCH_RESPONSE_RECEIVED.toString());
        acknowledgementDTO.setStatus(Constants.COMPLETED);
        return acknowledgementDTO;
    }
}
````

# Keycloak configuration
## Steps for DC and DP -
1. Create data-consumer/data-provider realm. Click on Add realm shown below. 
![Alt-text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-realm-creation.png)
2. Enter appropriate name in small cases.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-add-realm.png)
3. Change token expiry time as per requirement of testing. 
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-token-expiry.png)
4. Create client. 
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-create-realm-button.png)
5. Add appropriate client name.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-create-client.png)
6. Select Access type is confidential , enable service account and Enable Authorization. 
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-client-auth-setting.png)
7. Refer below image for client secret 
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-dc-client.png)
8. Enable above all setting for admin-cli client and admin cli of master realm aswell.
9. Add below scopes in client scope of admin-cli of master realm.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-master-admin-cli-client-scope.png)
10. Add below scopes in scope of admin-cli of master realm.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/keycloak-master-admin-cli-scope.png)


  



























