package g2pc.ref.farmer.regsvc.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.dto.request.QueryFarmerDTO;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import g2pc.core.lib.exceptionhandler.ErrorResponse;

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


    /**
     * Get search request from DC
     *
     * @param requestDTO required
     * @return Search request received acknowledgement
     * @throws JsonProcessingException the json processing exception
     * @throws ResponseStatusException the response status exception
     * @throws G2pcValidationException     the validation exception
     */
    @Operation(summary = "Receive search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/search")
    public AcknowledgementDTO registerCandidateInformation(@RequestBody RequestDTO requestDTO) throws JsonProcessingException, ResponseStatusException, G2pcValidationException {
        ObjectMapper objectMapper = new ObjectMapper();
        String cacheKey = Constants.CACHE_KEY_STRING + requestDTO.getMessage().getTransactionId();
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
    public ErrorResponse
    handleValidationException(
            G2pcValidationException ex)
    {
        return new ErrorResponse(
                ex.getG2PcErrorList());
    }
}
