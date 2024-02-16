package g2pc.ref.dc.client.service;

import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;
@Service
public interface DcValidationService {

    public void validateResponseDto(ResponseDTO responseDTO) throws Exception;

    ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception;

    StatusResponseMessageDTO signatureValidation(Map<String, Object> metaData, StatusResponseDTO statusResponseDTO) throws Exception;

    void validateStatusResponseDTO(StatusResponseDTO statusResponseDTO) throws IOException, G2pcValidationException;
}

