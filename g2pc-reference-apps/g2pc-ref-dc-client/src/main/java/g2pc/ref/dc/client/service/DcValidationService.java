package g2pc.ref.dc.client.service;

import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * The interface Dc validation service.
 */
@Service
public interface DcValidationService {

    public void validateResponseDto(ResponseDTO responseDTO) throws Exception;

    public void validateRegRecords(ResponseMessageDTO messageDTO) throws G2pcValidationException, IOException;

    ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception;
}
