package g2pc.ref.farmer.regsvc.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptionhandler.ValidationErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.BearerTokenUtil;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.dp.core.lib.repository.MsgTrackerRepository;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import g2pc.ref.farmer.regsvc.utils.DpCommonUtils;
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

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

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

    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    private DpCommonUtils dpCommonUtils;

    @Autowired
    private MsgTrackerRepository msgTrackerRepository;

    /**
     * Get search request from DC
     *
     * @param requestDTO required
     * @return Search request received acknowledgement
     */
    @Operation(summary = "Receive search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/public/api/v1/registry/search")
    public AcknowledgementDTO demoSearch(@RequestBody RequestDTO requestDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);

        RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);

        String cacheKey = Constants.CACHE_KEY_STRING + messageDTO.getTransactionId();

        return requestHandlerService.buildCacheRequest(objectMapper.writeValueAsString(requestDTO), cacheKey);
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
    @SuppressWarnings("unchecked")
    @Operation(summary = "Receive search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/search")
    public AcknowledgementDTO registerCandidateInformation(@RequestBody String requestString) throws Exception {
        dpCommonUtils.handleToken();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);

        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                readValue(requestString);
        RequestMessageDTO messageDTO = null;

        Map<String, Object> metaData = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();

        messageDTO = farmerValidationService.signatureValidation(metaData, requestDTO);
        requestDTO.setMessage(messageDTO);
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

    /**
     * Clear message tracker DB
     */
    @GetMapping("/private/api/v1/registry/clear-db")
    public void clearDb() throws G2pHttpException, IOException {
        dpCommonUtils.handleToken();
        msgTrackerRepository.deleteAll();
    }
}
