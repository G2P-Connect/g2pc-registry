package g2pc.ref.mno.regsvc.service;

import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import java.io.IOException;
import java.util.Map;

/**
 * The interface Mobile validation service.
 */
public interface MobileValidationService {

    void validateRequestDTO (RequestDTO requestDTO) throws Exception;

    RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO ) throws Exception;

    StatusRequestMessageDTO signatureValidation(Map<String, Object> metaData, StatusRequestDTO requestDTO) throws Exception ;

    void validateStatusRequestDTO (StatusRequestDTO requestDTO) throws IOException, G2pcValidationException;

}
