package g2pc.ref.dc.client.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseMessageDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptionhandler.ValidationErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.service.ElasticsearchService;
import g2pc.core.lib.service.SftpHandlerService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import g2pc.dc.core.lib.repository.ResponseDataRepository;
import g2pc.dc.core.lib.repository.ResponseTrackerRepository;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.ref.dc.client.config.JdbcConfig;
import g2pc.ref.dc.client.config.RegistryConfig;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.dto.dashboard.HttpsLeftPanelDataDTO;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import g2pc.ref.dc.client.service.DcSftpPushUpdateService;
import g2pc.ref.dc.client.service.DcValidationService;
import g2pc.ref.dc.client.utils.DcCommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kong.unirest.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
@RequestMapping(produces = "application/json")
@Tag(name = "Data Consumer", description = "DC APIs")
public class DcController {

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Autowired
    private DcRequestBuilderService dcRequestBuilderService;

    @Autowired
    private DcResponseHandlerService dcResponseHandlerService;

    @Autowired
    DcValidationService dcValidationService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    private DcCommonUtils commonUtils;

    @Autowired
    private ResponseDataRepository responseDataRepository;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    private RegistryConfig registryConfig;

    @Autowired
    private G2pUnirestHelper g2pUnirestHelper;

    @Autowired
    private SftpHandlerService sftpHandlerService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ResponseTrackerRepository responseTrackerRepository;

    @Autowired
    private DcSftpPushUpdateService dcSftpPushUpdateService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${spring.second-datasource.url}")
    private String url;

    @Value("${spring.second-datasource.username}")
    private String username;

    @Value("${spring.second-datasource.password}")
    private String password;

    @Value("${spring.second-datasource.driverClassName}")
    private String driverClassName;

    @Autowired
    private JdbcConfig jdbcConfig;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate=jdbcConfig.getJdbcTemplate();
    }

    /**
     * Get consumer search request
     *
     * @param payloadMap required
     * @return Search request received acknowledgement
     */
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
                    CoreConstants.SEND_PROTOCOL_HTTPS, "", "", "");
        }
        return acknowledgementDTO;
    }

    /**
     * Get consumer search request
     *
     * @param payloadFile required
     * @return Search request received acknowledgement
     */
    @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/public/api/v1/consumer/search/csv")
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

    /**
     * Listen to registry response
     *
     * @param responseString required
     * @return On-Search response received acknowledgement
     */
    @SuppressWarnings("unchecked")
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
                acknowledgementDTO = dcResponseHandlerService.getResponse(responseDTO, null, sunbirdEnabled);
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return acknowledgementDTO;
    }


    /**
     * Handle validation exception error response.
     *
     * @param ex the ValidationException
     * @return the error response
     */
    @ExceptionHandler(value
            = G2pcValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse
    handleValidationException(
            G2pcValidationException ex) {
        return new ValidationErrorResponse(
                ex.getG2PcErrorList());
    }

    /**
     * Handle validation exception error response.
     *
     * @param ex the ValidationException
     * @return the error response
     */
    @ExceptionHandler(value = G2pHttpException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleG2pHttpStatusException(G2pHttpException ex) {
        return new ErrorResponse(ex.getG2PcError());

    }

    /**
     * Clear transaction tracker DB
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/private/api/v1/registry/clear-db")
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
        try {
            jdbcTemplate.execute("DELETE FROM \"V_Response_Tracker\"");
            jdbcTemplate.execute("DELETE FROM \"V_Response_Data\"");
            jdbcTemplate.execute("DELETE FROM \"V_Msg_Tracker\"");
            jdbcTemplate.execute("DELETE FROM \"V_Txn_Tracker\"");
            log.info("Sunbird DB data cleared");
        } catch (Exception e) {
            log.error("Exception in clear Sunbird DB : ", e);
        }
        try {
            elasticsearchService.clearData("msg_tracker");
            elasticsearchService.clearData("txn_tracker");
            elasticsearchService.clearData("response_tracker");
            elasticsearchService.clearData("response_data");
            log.info("Sunbird elastic data cleared");
        } catch (Exception e) {
            log.error("Sunbird elastic data cleared: ", e);
        }
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("DC Redis cache cleared");
    }

    @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/consumer/status/payload")
    public AcknowledgementDTO createStatusRequest(@RequestParam String transactionId, @RequestParam String transactionType) throws Exception {
        log.info("Payload received for status");
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        if (ObjectUtils.isNotEmpty(transactionId)) {
            acknowledgementDTO = dcRequestBuilderService.generateStatusRequest(transactionId, transactionType, CoreConstants.SEND_PROTOCOL_HTTPS);
        }
        return acknowledgementDTO;
    }

    /**
     * Listen to registry response
     *
     * @param responseString required
     * @return On-status response received acknowledgement
     */
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

    /**
     * Listen to CSV file payload to handle using SFTP
     *
     * @param file required
     * @return AcknowledgementDTO
     */
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
            SftpServerConfigDTO sftpServerConfigDTO = commonUtils.getSftpConfigForDc();
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

    @GetMapping("/dashboard/leftPanel/data")
    public List<HttpsLeftPanelDataDTO> fetchLeftPanelData() {
        List<HttpsLeftPanelDataDTO> leftPanelDataDTOList;
        if (Boolean.FALSE.equals(sunbirdEnabled)) {
            leftPanelDataDTOList = new ArrayList<>();
            Optional<List<ResponseTrackerEntity>> optionalList = responseTrackerRepository.findAllByAction("search");
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
        } else {
            String sql = "SELECT message_ts, transaction_id, status FROM \"V_Response_Tracker\" WHERE action = 'search'";
            leftPanelDataDTOList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                HttpsLeftPanelDataDTO leftPanelDataDTO = new HttpsLeftPanelDataDTO();
                leftPanelDataDTO.setMessageTs(rs.getString("message_ts"));
                leftPanelDataDTO.setTransactionId(rs.getString("transaction_id"));
                leftPanelDataDTO.setStatus(rs.getString("status"));
                return leftPanelDataDTO;
            });
        }
        return leftPanelDataDTOList;
    }

    @GetMapping(value = "/dashboard/sftp/dc/data", produces = "text/event-stream")
    public SseEmitter sseEmitterFirstPanel() {
        return dcSftpPushUpdateService.register();
    }
}
