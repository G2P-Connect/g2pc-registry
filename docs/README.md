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
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/HeaderDTORelationship.png "a title")
- This module declares functionalities for token-based authentication, digital signature, and securing messages through encryption using various algorithms. 
- As per the requirement , these encryption and digital signature functions can be called in the below combination.  
  ![Alt text]( /home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/Sign-encry-table.png "a title")
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

  ![Alt text]( /home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/DC-DP-communication.png "a title")

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
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/search-endpoint-spec.png "a title")
  6. Once requestString gets validated data provider should save that data in redis cache and transaction data in db and send acknowledgement back to data consumer. Refer below sequence diagram for reference - 
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/search_sequence_diagram.png)
- Implementation explained in below point when it act like data consumer -
  1. When it acts like a consumer , it needs to define a scheduler. Scheduler is nothing but a framework that allows you to schedule and execute tasks at specific intervals or times. 
  2. In this scheduler , dp will check whether there is any data stored in pending status with a particular cache key corresponding to that data provider. 
  3. If it gets data it will build the response data and the call /on-search endpoint is defined in the data consumer . Please refer to Image 2 for the same. 
  4. Refer below for understanding of flow from dp to parent libraries. 
 ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-scheduler-sequence-dia.png)

# How to create a Data Provider ?
1. Create a spring boot application with the latest spring-boot version , maven and Java 17. And Click on generate to download.
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/spring_boot_dp_creation.png)
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
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/.p12-dp.png)
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
12. Add below autowired dependencies in the RegistryController class.
````
@Autowired
private RequestHandlerService requestHandlerService;

@Autowired
FarmerValidationService farmerValidationService;

@Autowired
G2pTokenService g2pTokenService;
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
 host: 3.109.26.38
 password: abhilash@99222
 port: 6378


client:
 api_urls:
   client_search_api: "http://localhost:8000/private/api/v1/registry/on-search"


keycloak:
 from-dc:
   url: "https://g2pc-dc-lab.cdpi.dev/auth/realms/data-consumer/protocol/openid-connect/token"
   client-id: dc-client
   client-secret: co0rJfm3mIq0OXysAt6DtDjibOHkcktY
 dp:
   master-admin-token-url: https://g2pc-dp1-lab.cdpi.dev/auth/realms/master/protocol/openid-connect/token
   get-client-url: https://g2pc-dp1-lab.cdpi.dev/auth/admin/realms/dp-farmer/clients
 realm: dp-farmer
 url: https://g2pc-dp1-lab.cdpi.dev/auth
 admin:
   realm:
     client-id: admin-cli
     client-secret: Sds0rtxBI4ChXKdVx2ytYsmvRmo9Jc2L  # In realm  admin-cli -> secret key
   master-client-id: admin-cli
   master-client-secret: G7rVA27HI5UpzMJfomRvaQHubtbAcWcN  # In realm In master admin-cli -> secret key
   username: admin
   password: cdpi@9923


crypto:
 to_dc:
   support_encryption: true
   support_signature: true
   password: "farmer_on_search"
   key.path: "classpath:farmer_on_search.p12"
   id: FARMER
 from_dc:
   support_encryption: true
   support_signature: true
   password: "farmer_search"
   key.path: "classpath:farmer_search.p12"


dashboard:
 dp_dashboard_url: "http://3.109.26.38:3005/d-solo/e62ae08b-a6e1-4095-af79-c36f02b8fae2/dp1-dashboard?orgId=1&refresh=5s&from=1701984074137&to=1702005674137&panelId=1"
````
14. Add below attributes as per your requirement -  
    1. Change db name (gtwop) , schema name (farmer) , username and password for db connection as per your postgres/mysql connection. 
    2. client.api_urls.client_search_api -> change port as  respective on-search api. 
    3. keycloak.from-dc.url -> create realm for data consumer and replace name with keycloak data-consumer realm name. 
    4. keycloak.from-dc.client-id -> respective client id of created realm. 
    5. keycloak.from-dc.client.secret -> respective client secret of created realm. 
    6. keycloak.dp.admin-url -> replace port with your keycloak port 
    7. keycloak.dp.get-client-url -> replace port with your keycloak port and name with respective dp realm name. 
    8. keycloak.realm -> add respective dp realm name 
    9. keycloak.url -> add keycloak url 
    10. keycloak.admin.realm.client-id -> respective dp realm's admin_cli client id
    11. keycloak.admin.realm.client-secret -> respective realm's admin_cli client secret 
    12. keycloak.admin.master-client-cli -> In respective keycloak instance  , master realm’s admin_cli client-id 
    13. keyloak.admin.master-client-secret -> In respective keycloak instance  , master realm’s admin_cli client-secret 
    14. keycloak.admin.username -> Respective keycloak instance username 
    15. keycloak.admin.password -> Respective keycloak instance password 
    16. crypto.to_dc.support_encryption -> flag of encryption when scheduler will call /on-search api 
    17. crypto.to_dc.support_signature -> flag of signature when scheduler will call /on-search api 
    18. crypto.to_dc.password -> password of on_search.p12 password 
    19. crypto.to_dc.key.path -> path of on_search.p12 file 
    20. crypto.to_dc.id -> flag of Farmer id for data consumer. 
    21. crypto.from_dc.support_encryption ->flag of encryption when the data consumer will call /search to validate the configuration. 
    22. crypto.from_dc.support_signature ->  flag of signature when the data consumer will call /search to validate the configuration. 
    23. crypto.from_dc.password -> password of search.p12 password 
    24. crypto.from_dc.key.path -> path of search.p12 file 
    25. Dashboard.dp_dashboard_url ->
15. Add below values in RegistryController.
````
@Value("${keycloak.realm}")
private String keycloakRealm;


@Value("${keycloak.url}")
private String keycloakURL;


@Value("${keycloak.dp.master-admin-token-url}")
private String masterAdminUrl;


@Value("${keycloak.dp.get-client-url}")
private String getClientUrl;


@Value("${keycloak.admin.realm.client-id}")
private String realmAdmin_cliClientId;


@Value("${keycloak.admin.realm.client-secret}")
private String realmAdmin_cliClientSecret;


@Value("${keycloak.admin.master-client-id}")
private String masterAdmin_cliClientId;


@Value("${keycloak.admin.master-client-secret}")
private String masterAdmin_clientSecret;


@Value("${keycloak.admin.username}")
private String adminUsername;


@Value("${keycloak.admin.password}")
private String adminPassword;


@Value("${crypto.to_dc.support_encryption}")
private boolean isEncrypt;


@Value("${crypto.to_dc.support_signature}")
private boolean isSign;

````
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
17. Add below code snippet in RegistryController  for authentication in which parent g2pc.core library’s methods have been called. This will authenticate the user whether its token is valid or not and whether it has been created from keycloak or not using introspect url.
````
String token = BearerTokenUtil.getBearerTokenHeader();
String introspect = keycloakURL+"/realms/"+keycloakRealm+"/protocol/openid-connect/token/introspect";
ResponseEntity<String> introspectResponse =  g2pTokenService.getInterSpectResponse(introspect,token, realmAdmin_cliClientId, realmAdmin_cliClientSecret);
if(introspectResponse.getStatusCode().value()==401){
   throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(),introspectResponse.getBody()));
}
if(!g2pTokenService.validateToken(masterAdminUrl,getClientUrl , g2pTokenService.decodeToken(token) , masterAdmin_cliClientId, masterAdmin_clientSecret, adminUsername , adminPassword)){
   throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_USER_UNAUTHORIZED.toValue(), "User is not authorized"));
}

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
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp_schema.png)
29. With reference to below Query of farmer data provider. Refer specification -
    [specification](https://g2p-connect.github.io/specs/release/html/registry_core_api_v1.0.0.html#tag/Async/operation/post_reg_search)
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-specs-json.png)
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/dp-specs.png)
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
37. Add below code snippet to in ResponseBuilderServiceImpl
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
                        Map<String , Object> meta= (Map<String, Object>) headerDTO.getMeta().getData();
                        meta.put(CoreConstants.DP_ID,dp_id);
                        requestDTO.getHeader().getMeta().setData(meta);
                        String responseString = responseBuilderService.buildResponseString("signature",
                                headerDTO, responseMessageDTO);
                        responseString = CommonUtils.formatString(responseString);
                        log.info("on-search response = {}", responseString);
                        Resource resource = resourceLoader.getResource(farmer_key_path);
                        String encryptedSalt="";
                        InputStream fis = resource.getInputStream();
                        G2pcError g2pcError = responseBuilderService.sendOnSearchResponse(responseString, onSearchURL, dcClientId, dcClientSecret, keyClockClientTokenUrl , fis , encryptedSalt);
                        if (!g2pcError.getCode().equals(HttpStatus.OK.toString())) {
                            throw new G2pHttpException(g2pcError);
                        } else {
                            txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
    
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
![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/patload_postman.png)
  2. Using this data consumer will decide which data provider’s endpoint it needs to call and which request it needs to build.
  3. Once a request is created /search endpoint it will call and once positive acknowledgement is there it will save pending status in cache for particular transaction id. Refer below for more understanding.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/payload_sequence_diagram.png)
- Implementation explained in below point when it act like data provider -
  1. As shown in Figure 2 data provider needs to implement the end point and also make calls to the endpoint of the data provider. 
  2. At first Data consumer service needs to write the /on-search end-point. 
  3. This endpoint will receive a responseString transferred by the data provider. 
  4. In this endpoint authentication also needs to be defined to ensure that the correct user is accessing the endpoint or not. 
  5. Also need to make sure that the correct signature and valid message is received. 
  6. This responseString will get validated as per g2p specification. Please refer to the link mentioned and image below.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/on_search_spec.png)
  7. Once responseString gets validated data consumers should update that data in redis cache and send acknowledgement back to the data consumer. Refer below for more understanding.
  ![Alt text](/home/ttpl-rt-119/Documents/CDPI/G2P-Code/Git_hub/g2pc-registry/docs/src/images/on_search_seqeunce_dia.png)
  



























