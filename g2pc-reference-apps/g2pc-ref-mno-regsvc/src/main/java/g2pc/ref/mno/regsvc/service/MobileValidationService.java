package g2pc.ref.mno.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import java.util.Map;

/**
 * The interface Mobile validation service.
 */
public interface MobileValidationService {

    void validateRequestDTO (RequestDTO requestDTO) throws Exception;

    void validateQueryDto (QueryDTO queryMobileDTO) throws G2pcValidationException, JsonProcessingException;

    RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO ) throws Exception;
}
