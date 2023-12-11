package g2pc.ref.farmer.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import java.util.Map;
/**
 * The interface Farmer validation service.
 */
public interface FarmerValidationService {

    void validateRequestDTO (RequestDTO requestDTO) throws Exception;

    void validateQueryDto (QueryDTO queryFarmerDTO) throws G2pcValidationException, JsonProcessingException;

    RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO ) throws Exception;
}
