package g2pc.ref.farmer.regsvc.controller.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptionhandler.ValidationErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * The type Registry controller.
 */
@RestController
@Slf4j
@RequestMapping(produces = "application/json")
@Tag(name = "Provider", description = "Provider APIs")
public class RegistryController {

    @Autowired
    private RequestHandlerService requestHandlerService;

    /**
     * The Farmer validation service.
     */
    @Autowired
    FarmerValidationService farmerValidationService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    G2pTokenService g2pTokenService;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.url}")
    private String keycloakURL;

    @Value("${keycloak.farmer.admin-url}")
    private String masterAdminUrl;

    @Value("${keycloak.farmer.get-client-url}")
    private String getClientUrl;

    @Value("${crypto.support_encryption}")
    private String isEncrypt;

    @Value("${crypto.support_signature}")
    private String isSign;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    @Operation(summary = "Receive search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/public/api/v1/registry/search")
    public AcknowledgementDTO demoSearch(@RequestBody String requestString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                readValue(requestString);
       RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
        String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
        return requestHandlerService.buildCacheRequest(
                objectMapper.writeValueAsString(requestDTO), cacheKey);
    }

    /**
     * Get search request from DC
     *
     * @param requestString required
     * @return Search request received acknowledgement
     * @throws JsonProcessingException the json processing exception
     * @throws ResponseStatusException the response status exception
     * @throws G2pcValidationException the validation exception
     */
    @Operation(summary = "Receive search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/search")
    public AcknowledgementDTO getRequestForSearch(@RequestBody String requestString) throws Exception {
        String token = BearerTokenUtil.getBearerTokenHeader();
        String introspect = keycloakURL + "/realms/" + keycloakRealm + "/protocol/openid-connect/token/introspect";
        ResponseEntity<String> introspectResponse = g2pTokenService.getInterSpectResponse(introspect, token, adminClientId, adminClientSecret);
        if (introspectResponse.getStatusCode().value() == 401) {
            throw new G2pHttpException(new G2pcError(introspectResponse.getStatusCode().toString(), introspectResponse.getBody()));
        }
        if (!g2pTokenService.validateToken(masterAdminUrl, getClientUrl, g2pTokenService.decodeToken(token))) {
            throw new G2pHttpException(new G2pcError("err.request.unauthorized", "User is not authorized"));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);

        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                readValue(requestString);
        RequestMessageDTO messageDTO = null;

        if (isEncrypt.equals("true")) {
            String messageString = requestDTO.getMessage().toString();
            String deprecatedMessageString = encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
            messageDTO = objectMapper.readerFor(RequestMessageDTO.class).
                    readValue(deprecatedMessageString);
        } else {
            messageDTO = objectMapper.readerFor(RequestMessageDTO.class).
                    readValue((JsonParser) requestDTO.getMessage());
        }

        String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();
        try {
            farmerValidationService.validateRequestDTO(requestDTO);
            return requestHandlerService.buildCacheRequest(
                    objectMapper.writeValueAsString(requestDTO), cacheKey);
        } catch (G2pcValidationException e) {
            throw new G2pcValidationException(e.getG2PcErrorList());
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * TokenIsNotValidException
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
