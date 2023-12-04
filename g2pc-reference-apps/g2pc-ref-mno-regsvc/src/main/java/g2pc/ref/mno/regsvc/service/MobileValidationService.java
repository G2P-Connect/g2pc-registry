package g2pc.ref.mno.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The interface Mobile validation service.
 */
public interface MobileValidationService {

    /**
     * Validate request dto.
     *
     * @param requestDTO the request dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    void validateRequestDTO (RequestDTO requestDTO) throws Exception;

    /**
     * Validate query dto.
     *
     * @param queryMobileDTO the query mobile dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    void validateQueryDto (QueryDTO queryMobileDTO) throws G2pcValidationException, JsonProcessingException;
   }
