package g2pc.ref.dc.client.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.G2pSecurityConstants;
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
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import g2pc.ref.dc.client.service.DcValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    G2pTokenService g2pTokenService;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.url}")
    private String keycloakURL;

    @Value("${keycloak.consumer.admin-url}")
    private String masterAdminUrl;

    @Value("${keycloak.consumer.get-client-url}")
    private String getClientUrl;

    @Value("${crypto.support_encryption}")
    private String isEncrypt;

    @Value("${crypto.support_signature}")
    private String isSign;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

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
            acknowledgementDTO = dcRequestBuilderService.generateRequest(Collections.singletonList(payloadMap));
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
            acknowledgementDTO = dcRequestBuilderService.generatePayloadFromCsv(payloadFile);
        }
        return acknowledgementDTO;
    }

    @PostMapping("/public/api/v1/registry/on-search")
    public AcknowledgementDTO demoOnSearch(@RequestBody String responseString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        ResponseDTO responseDTO = objectMapper.readerFor(ResponseDTO.class).
                readValue(responseString);
        return dcResponseHandlerService.getResponse(responseDTO);
    }

    /**
     * Listen to registry response
     *
     * @param responseString required
     * @return On-Search response received acknowledgement
     */
    @Operation(summary = "Listen to registry response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.ON_SEARCH_RESPONSE_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/on-search")
    public AcknowledgementDTO createSearchRequests(@RequestBody String responseString) throws Exception {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/realms/" + keycloakRealm + "/protocol/openid-connect/token/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, adminClientId, adminClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterAdminUrl, getClientUrl, g2pTokenService.decodeToken(token))) {
            throw new G2pHttpException(new G2pcError("err.request.unauthorized", "User is not authorized"));
        }

        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        ResponseDTO responseDTO = objectMapper.readerFor(ResponseDTO.class).
                readValue(responseString);
        ResponseMessageDTO messageDTO = null;
        if (isEncrypt.equals("true")) {
            String messageString = objectMapper.convertValue(responseDTO.getMessage(), String.class);
            String deprecatedMessageString = encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
            messageDTO = objectMapper.readerFor(ResponseMessageDTO.class).
                    readValue(deprecatedMessageString);
        } else {
            messageDTO = objectMapper.convertValue(responseDTO.getMessage(), ResponseMessageDTO.class);
        }
        try {
            dcValidationService.validateResponseDto(responseDTO);
            if (ObjectUtils.isNotEmpty(responseDTO)) {
                acknowledgementDTO = dcResponseHandlerService.getResponse(responseDTO);
            }
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
}
