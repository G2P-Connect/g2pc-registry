package g2pc.ref.farmer.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.ref.farmer.regsvc.dto.request.QueryFarmerDTO;

/**
 * The interface Farmer validation service.
 */
public interface FarmerValidationService {

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
     * @param queryFarmerDTO the query farmer dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    void validateQueryDto (QueryFarmerDTO queryFarmerDTO) throws G2pcValidationException, JsonProcessingException;
}
