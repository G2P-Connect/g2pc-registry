package g2pc.ref.dc.client.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptionhandler.ValidationErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.dc.core.lib.repository.ResponseDataRepository;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.ref.dc.client.config.RegistryConfig;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import g2pc.ref.dc.client.service.DcValidationService;
import g2pc.ref.dc.client.utils.DcCommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@RestController
@Slf4j
@RequestMapping(produces = "application/json")
@Tag(name = "Data Consumer", description = "DC APIs")
public class DcController {

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
    public Map<String, G2pcError> createSearchRequestsFromPayload(@RequestBody Map<String, Object> payloadMap) throws Exception {
        log.info("Payload received from postman");
        Map<String, G2pcError> acknowledgement = new HashMap<>();
        if (ObjectUtils.isNotEmpty(payloadMap)) {
            acknowledgement = dcRequestBuilderService.generateRequest(Collections.singletonList(payloadMap));
        }
        return acknowledgement;
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
    public Map<String, G2pcError> createSearchRequestsFromCsv(@RequestPart(value = "file") MultipartFile payloadFile) throws Exception {
        Map<String, G2pcError> acknowledgement = new HashMap<>();
        log.info("Payload received from csv file");
        if (ObjectUtils.isNotEmpty(payloadFile)) {
            acknowledgement = dcRequestBuilderService.generatePayloadFromCsv(payloadFile);
        }
        //TODO: convert returning map to acknowledgementDTO
        return acknowledgement;
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
                acknowledgementDTO = dcResponseHandlerService.getResponse(responseDTO);
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

        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig().entrySet()) {
            try {
                Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig().get(configEntryMap.getKey());
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
                log.error("Exception in clearDb: {}", e);
            }
        }
    }
}
