package g2pc.ref.mno.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;

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
    void validateRequestDTO (RequestDTO requestDTO) throws G2pcValidationException, JsonProcessingException;

    /**
     * Validate query dto.
     *
     * @param queryMobileDTO the query mobile dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    void validateQueryDto (QueryMobileDTO queryMobileDTO) throws G2pcValidationException, JsonProcessingException;

}
