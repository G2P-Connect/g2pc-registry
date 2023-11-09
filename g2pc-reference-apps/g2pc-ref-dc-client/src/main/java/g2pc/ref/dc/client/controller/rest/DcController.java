package g2pc.ref.dc.client.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.dto.payload.PayloadDTO;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import g2pc.ref.dc.client.service.DcValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

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

    /**
     * Get consumer search request
     *
     * @param payloadDTO required
     * @return Search request received acknowledgement
     */
    @Operation(summary = "Receive consumer search request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SEARCH_REQUEST_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/consumer/search")
    public AcknowledgementDTO createSearchRequests(@RequestBody PayloadDTO payloadDTO) throws JsonProcessingException {
        log.info("Payload received");
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        if (ObjectUtils.isNotEmpty(payloadDTO.getData())) {
            acknowledgementDTO = dcRequestBuilderService.generateRequest(new ObjectMapper().writeValueAsString(payloadDTO));
        }
        return acknowledgementDTO;
    }

    /**
     * Listen to registry response
     *
     * @param responseDTO required
     * @return On-Search response received acknowledgement
     */
    @Operation(summary = "Listen to registry response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.ON_SEARCH_RESPONSE_RECEIVED),
            @ApiResponse(responseCode = "401", description = Constants.INVALID_AUTHORIZATION),
            @ApiResponse(responseCode = "403", description = Constants.INVALID_RESPONSE),
            @ApiResponse(responseCode = "500", description = Constants.CONFLICT)})
    @PostMapping("/private/api/v1/registry/on-search")
    public AcknowledgementDTO createSearchRequests(@RequestBody ResponseDTO responseDTO) throws G2pcValidationException{
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
      try {
          dcValidationService.validateResponseDto(responseDTO);
          if (ObjectUtils.isNotEmpty(responseDTO)) {
              acknowledgementDTO = dcResponseHandlerService.getResponse(responseDTO);
          }
      } catch (G2pcValidationException e) {

          throw new G2pcValidationException(e.getG2PcErrorList());
      }
      catch (JsonProcessingException e){
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR , e.getMessage());
      }
      catch (Exception e){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST , e.getMessage());
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
    public ErrorResponse
    handleValidationException(
            G2pcValidationException ex)
    {
        return new ErrorResponse(
                ex.getG2PcErrorList());
    }
}
